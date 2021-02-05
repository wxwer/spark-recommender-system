package com.wang.factorization

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.DataFrame
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.recommendation.Rating
import org.apache.spark.mllib.recommendation.ALS
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.jblas.DoubleMatrix

object ProductRecommend {
  val MONGODB_RATING_COLLECTION="Rating";
  val FACTOR_PRODUCT_RECS = "ProductRecs";
  
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
    //使用最小二乘法进行矩阵分解
    val (rank,iter,lambda)=(5,10,0.1);
    val model = ALS.train(ratingRDD, rank,iter,lambda);
    //model.productFeatures.toDF().show();
    
    //获取商品的特征矩阵
    val productFeatures:RDD[(Int,DoubleMatrix)]=model.productFeatures.map({
      case (productId,features) => (productId,new DoubleMatrix(features));
    });
    //productFeatures.toDF().show();
    //两两配对计算余弦相似度(自己跟自己作笛卡尔积)
    val productResDF:DataFrame=productFeatures.cartesian(productFeatures)
      .filter{
        case (a,b)=> a._1!=b._1
      }
      .map{
        case (a,b)=>
          val similarity=cosineSimilarity(a._2,b._2);
          (a._1,(b._1,similarity));
      }
      .filter(_._2._2>0.4)
      .groupByKey()
      .map{
        case (productId,resc)=>
          RecommendationProduct(productId,resc.toList.sortWith(_._2>_._2).map(x=>Recommendation(x._1,x._2)));
      }.toDF();
    
      OperateMongoDb.dataFrameToSave(productResDF, FACTOR_PRODUCT_RECS);
      ratingRDD.unpersist();
      sparkSession.stop();
      
  }
    /**
    * 余弦相似度
    *
    * @param vec1
    * @param vec2
    * @return
    */
  private def cosineSimilarity(vec1: DoubleMatrix, vec2: DoubleMatrix): Double = {
    vec1.dot(vec2) / (vec1.norm2() * vec2.norm2())
  }
}