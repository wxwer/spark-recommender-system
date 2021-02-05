package com.wang.usercf


import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.DataFrame

/**
 * 基于用户的协同过滤
 * @author wang
 * @time 2021/01/06
 */
object UserCfRecommend {
  //定义表名及常量名
  val MONGODB_RATING_COLLECTION="Rating";
  val USER_CF_PRODUCT_RECS="UserCFProductRecs";
  val MAX_RECOMMENDATION=500;
  
  def main(args:Array[String]):Unit={
    //加载sparkSession
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
    //读取评分数据
    val ratingDF:DataFrame=OperateMongoDb.loadInfoAsDataFrame(sparkSession, MONGODB_RATING_COLLECTION).as[Rating].map(
      x=>(x.userId,x.productId,x.score)
      )
      .toDF("userId","productId","score")
      .cache();
    //统计每个用户出现次数
    val userRatingCountDF:DataFrame=ratingDF.groupBy("userId").count();
    //userRatingCountDF.show();
    val ratingOfCountDF:DataFrame=ratingDF.join(userRatingCountDF,"userId");
    //计算两两用户共同出现的次数
    val joinDF:DataFrame=ratingOfCountDF.join(ratingOfCountDF, "productId")
      .toDF("productId","user1","score1","count1","user2","score2","count2")
      .select("productId","user1","count1","user2","count2");
    joinDF.createOrReplaceTempView("joined");
    val coocurrenceDF:DataFrame=sparkSession.sql(
        """
          |select user1,user2,count(productId) as cocount,first(count1) as count1,first(count2) as count2
          |from joined group by user1,user2
        """.stripMargin
        ).cache();
    
    //joinDF.show();
    //计算共现度相似性，并包装推荐结果
    val simDF:DataFrame=coocurrenceDF.map{
      row=>
        val cooSim=cooccurrenceSim(row.getAs[Long]("cocount"), row.getAs[Long]("count1"), row.getAs[Long]("count2"));
        (row.getInt(0),(row.getInt(1),cooSim));
    }
      .rdd
      .groupByKey()
      .map{
        case (userId,recs)=>
          RecommendationUser(userId,recs.toList.filter(_._1!=userId)
              .sortWith(_._2>_._2)
              .take(MAX_RECOMMENDATION)
              .map(x=>Recommendation(x._1,x._2)));
      }
      .toDF();
    //simDF.show();
    //保存结果
    OperateMongoDb.dataFrameToSave(simDF, USER_CF_PRODUCT_RECS)
    coocurrenceDF.unpersist();
    ratingDF.unpersist();
    sparkSession.stop();
  }
  def cooccurrenceSim(coCount:Long,count1:Long,count2:Long):Double={
    coCount/math.sqrt(count1*count2);
  }
}