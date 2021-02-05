package com.wang.statistics

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.DataFrame
import java.text.SimpleDateFormat
import java.util.Date


object Statistics {
  
  /**
    * 原始数据 商品 评分 表明
    */
  val MONGODB_RATING_COLLECTION="Rating";
  val MONGODB_PRODUCT_COLLECTION="Product";
  /**
   * 商品历史评分次数表
   */
  val RATE_MORE_PRODUCTS="RateMoreProducts";
  /**
   * 近期热门商品
   */
  val RATE_MORE_RECENTLY_PRODUCTS="RateMoreRecentlyProducts";
  /**
   * 商品平均评分表
   */
  val AVERAGE_SCORE_PRODUCTS="AverageScoreProducts";
  
  def main(args:Array[String]):Unit={
    val config:Map[String,String]=Map(
        "spark.cores"->"local[*]",
        "mongo.url"->"mongodb://127.0.0.1:27017/test",
        "mongo.db"->"test"
        );
    val sparkConf:SparkConf=new SparkConf().setAppName(this.getClass.getSimpleName).setMaster(config("spark.cores"));
    val sparkSession:SparkSession=SparkSession.builder()
      .config(sparkConf)
      .getOrCreate();
    import sparkSession.implicits._;
    implicit val mongoConfig=MongoConfig(config("mongo.url"),config("mongo.db"));
    
    val ratingDF:DataFrame=OperateMongoDb.loadInfoAsDataFrame(sparkSession, MONGODB_RATING_COLLECTION).as[Rating].toDF();
    ratingDF.createOrReplaceTempView("rating");
    //统计商品历史评分次数，并按次数从大到小排序
    val rateMoreProducts:DataFrame=sparkSession.sql("select productId,count(productId) as count from rating group by productId order by count desc");
    OperateMongoDb.dataFrameToSave(rateMoreProducts, RATE_MORE_PRODUCTS);
    //转换时间格式
    val simpleDateFormat:SimpleDateFormat=new SimpleDateFormat("yyyyMM");
    sparkSession.udf.register("changeDate", (x:Int)=>simpleDateFormat.format(new Date(x*1000L)).toInt);
    val ratingOfYearMonthDF=sparkSession.sql("select productId,score,changeDate(timestamp) as yearmonth from rating");
    ratingOfYearMonthDF.createOrReplaceTempView("ratingOfMonth");
    //统计每月商品评分次数
    val rateMoreRecentlyProductsDF=sparkSession.sql("select productId,count(productId) as count,yearmonth from ratingOfMonth group by yearmonth,productId order by yearmonth desc,count desc");
    OperateMongoDb.dataFrameToSave(rateMoreRecentlyProductsDF,RATE_MORE_RECENTLY_PRODUCTS);
    //统计商品平均得分
    val averageScoreProductsDF=sparkSession.sql("select productId,avg(score) as score from rating group by productId order by score desc");
    OperateMongoDb.dataFrameToSave(averageScoreProductsDF, AVERAGE_SCORE_PRODUCTS);
    
    sparkSession.stop();
    
  }
}