package com.wang.factorization

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.DataFrame
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.recommendation.Rating
import org.apache.spark.mllib.recommendation.ALS
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel

object UserRecommend {
  //表常量定义
  val MONGODB_RATING_COLLECTION="Rating";
  val USER_RECS = "UserRecs";
  //每个用户最大的推荐商品数
  val USER_MAX_RECOMMENDATION = 500;
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
    //读取评分数据，并转换成RDD
    val ratingRDD:RDD[Rating]=OperateMongoDb.loadInfoAsDataFrame(sparkSession, MONGODB_RATING_COLLECTION).as[ProductRating]
      .rdd
      .map(
      x=>Rating(x.userId,x.productId,x.score)
      )
      .cache();
    //获取userId和productId的集合
    val userRDD:RDD[Int] = ratingRDD.map(_.user).distinct();
    val productRDD:RDD[Int] = ratingRDD.map(_.product).distinct();
    
    //使用最小二乘法进行矩阵分解
    val (rank,iter,lambda)=(5,10,0.1);
    val model = ALS.train(ratingRDD, rank,iter,lambda);
    //预测每个用户与商品的评分
    val userProducts:RDD[(Int,Int)] = userRDD.cartesian(productRDD);
    val preRatingRDD:RDD[Rating] = model.predict(userProducts);
    //对结果进行包装
    val userRecs: DataFrame = preRatingRDD
      .filter(_.rating>1)
      .map(x=>(x.user,(x.product,x.rating)))
      .groupByKey()
      .map{
      case (userId,recs) =>
        RecommendationUser(userId,recs.toList.sortWith(_._2>_._2).take(USER_MAX_RECOMMENDATION).map(x=>Recommendation(x._1,x._2)));
    }.toDF();
    //保存结果
    OperateMongoDb.dataFrameToSave(userRecs, USER_RECS);
    ratingRDD.unpersist();
    sparkSession.stop();
  }
}