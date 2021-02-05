package com.wang.content
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.DataFrame
import org.apache.spark.ml.feature.Tokenizer
import org.apache.spark.sql.DataFrameReader
import org.apache.spark.ml.feature.HashingTF
import org.apache.spark.ml.feature.IDF
import org.apache.spark.ml.feature.IDFModel
import org.jblas.DoubleMatrix
import org.apache.spark.rdd.RDD
import org.apache.spark.ml.linalg.SparseVector


object ContentRecommend {
  val MONGODB_PRODUCT_COLLECTION="Product";
  val CONTENT_PRODUCT_RECS="ContentBasedProductRecs";
  
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
    val productDF:DataFrame=OperateMongoDb.loadInfoAsDataFrame(sparkSession, MONGODB_PRODUCT_COLLECTION).as[Product].map{
      x=>
        (x.productId,x.name,x.tags.map(c=>if(c=='|') ' ' else c))
    }.toDF("productId","name","tags").cache();
    
    val tokenizer:Tokenizer=new Tokenizer().setInputCol("tags").setOutputCol("words");
    val wordsDataDF:DataFrame=tokenizer.transform(productDF);
    
    val hashingTF:HashingTF=new HashingTF().setInputCol("words").setOutputCol("rawFeatures").setNumFeatures(800);
    val featureDF:DataFrame=hashingTF.transform(wordsDataDF);
    
    val idf: IDF=new IDF().setInputCol("rawFeatures").setOutputCol("features");
    val iDFModel:IDFModel=idf.fit(featureDF);
    //featureDF.show();
    val rescaledDataDF:DataFrame=iDFModel.transform(featureDF);
    val productFeatures:RDD[(Int,DoubleMatrix)]=rescaledDataDF.map{
      row=>(row.getAs[Int]("productId"),row.getAs[SparseVector]("features").toArray)
    }
    .rdd
    .map{
      case (productId,features) =>(productId,new DoubleMatrix(features))
    };
    
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
    //productResDF.show();
    OperateMongoDb.dataFrameToSave(productResDF,CONTENT_PRODUCT_RECS);
    productDF.unpersist();
    sparkSession.stop();
  }
  /**
    * 余弦相似度
    *
    * @param vec1
    * @param vec2
    * @return
    */
  private def cosineSimilarity(vec1:DoubleMatrix,vec2:DoubleMatrix):Double={
    vec1.dot(vec2)/(vec1.norm2()*vec2.norm2())
  }
}