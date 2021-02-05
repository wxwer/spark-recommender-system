package com.wang.factorization

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.DataFrame
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.recommendation.Rating
import org.apache.spark.mllib.recommendation.ALS
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel

object ALSTrainer {
  val MONGODB_RATING_COLLECTION="Rating";
  
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
    //将评分数据划分为训练集和测试集
    val splits:Array[RDD[Rating]]=ratingRDD.randomSplit(Array(0.8,0.2), 35);
    val trainingRDD=splits(0);
    val testingRDD=splits(1);
    
    //testingRDD.toDF().show();
    //调整参数
    adjustALSParams(trainingRDD, testingRDD);
    sparkSession.stop();
    
  }
  def adjustALSParams(trainingData:RDD[Rating],testingData:RDD[Rating]):Unit={
    //遍历参数，包含向量维数和lambda参数
    val result = for(rank <- Array(5,10,20,50);lambda <- Array(1,0.1,0.01))
      yield {
        val model = ALS.train(trainingData, rank, 10,lambda);
        val rmse = getRMSE(model, testingData);
        (rank,lambda,rmse);
    }
    println(result.minBy(_._3));
  }
  
  //计算均方根误差
  def getRMSE(model:MatrixFactorizationModel,testingData:RDD[Rating]):Double={
    val userProductRDD:RDD[(Int,Int)]=testingData.map(x=>(x.user,x.product));
    val predictRatingRDD:RDD[Rating] = model.predict(userProductRDD);
    
    val observedRDD:RDD[((Int,Int),Double)] = testingData.map(x=>((x.user,x.product),x.rating));
    val predictedRDD:RDD[((Int,Int),Double)] = predictRatingRDD.map(x=>((x.user,x.product),x.rating));
    math.sqrt(
        observedRDD.join(predictedRDD).map{
          case ((userId,productId),(actual,pred)) =>
            val err = pred - actual;
            err * err;
        }.mean()
    )
  }
}