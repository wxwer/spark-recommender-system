package com.wang.dataload

import org.apache.spark.SparkConf
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.DataFrame


object DataLoad {
  val PRODUCT_PATH="./src/main/resources/products.csv";
  val RATING_PATH="./src/main/resources/ratings.csv";
  val USER_PATH="./src/main/resources/users.csv";
  
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
    val productDF:DataFrame=sparkSession.sparkContext.textFile(PRODUCT_PATH).map(item=>{
      val attr:Array[String]=item.split(",");
      Product(attr(0).toInt,attr(1).trim(),attr(2).trim(),attr(3).trim(),attr(attr.length-1).trim())
    }).toDF();
    val ratingDF:DataFrame=sparkSession.sparkContext.textFile(RATING_PATH).map(item=>{
      val attr:Array[String]=item.split(",");
      Rating(attr(0).toInt,attr(1).toInt,attr(2).toDouble,attr(3).toInt)
    }).toDF();
    val userDF:DataFrame=sparkSession.sparkContext.textFile(USER_PATH).map(item=>{
      val attr:Array[String]=item.split(",");
      User(attr(0).toInt,attr(1).toInt,attr(2).trim(),attr(3).trim())
    }).toDF();
    productDF.cache();
    ratingDF.cache();
    userDF.cache();
    
    implicit val mongoConfig=MongoConfig(config("mongo.url"),config("mongo.db"));
    
    CreateDataRecord.storeDataInMongo(productDF, ratingDF,userDF);
    
    productDF.unpersist();
    ratingDF.unpersist();
    userDF.unpersist();
    
    sparkSession.stop();
    
  }
}