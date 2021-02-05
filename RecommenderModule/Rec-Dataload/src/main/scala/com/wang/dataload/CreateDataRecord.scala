package com.wang.dataload

import org.apache.spark.sql.DataFrame
import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.MongoClientURI
import com.mongodb.casbah.commons.MongoDBObject

object CreateDataRecord {
  // Product在MongoDB中的Collection名称【表】
  val PRODUCTS_COLLECTION_NAME="Product";
  // Rating在MongoDB中的Collection名称【表】
  val RATINGS_COLLECTION_NAME="Rating";
  
  // user在MongoDB中的Collection名称【表】
  val USERS_COLLECTION_NAME="User";
  /**
    * Store Data In MongoDB
    *
    * @param products    商品数据集
    * @param ratings     评分数据集
    * @param mongoConfig MongoDB的配置
    */
  def storeDataInMongo(products:DataFrame,ratings:DataFrame,users:DataFrame)(implicit mongoConfig:MongoConfig):Unit={
    // 建立连接
    val mongoClient:MongoClient=MongoClient(MongoClientURI(mongoConfig.url));
    // 删除products的Collection
    mongoClient(mongoConfig.dbName)(PRODUCTS_COLLECTION_NAME).dropCollection();
    // 删除Rating的Collection
    mongoClient(mongoConfig.dbName)(RATINGS_COLLECTION_NAME).dropCollection();
    
    // 删除User的Collection
    mongoClient(mongoConfig.dbName)(USERS_COLLECTION_NAME).dropCollection();
    
    // 写入products信息
    products.write
      .option("uri", mongoConfig.url)
      .option("collection", PRODUCTS_COLLECTION_NAME)
      .mode("overwrite")
      .format("com.mongodb.spark.sql")
      .save();
    
    // 写入ratings信息
    ratings.write
      .option("uri", mongoConfig.url)
      .option("collection", RATINGS_COLLECTION_NAME)
      .mode("overwrite")
      .format("com.mongodb.spark.sql")
      .save();
    
    // 写入users信息
    users.write
      .option("uri", mongoConfig.url)
      .option("collection", USERS_COLLECTION_NAME)
      .mode("overwrite")
      .format("com.mongodb.spark.sql")
      .save();
    
    mongoClient(mongoConfig.dbName)(PRODUCTS_COLLECTION_NAME).createIndex(MongoDBObject("productId"->1));
    mongoClient(mongoConfig.dbName)(RATINGS_COLLECTION_NAME).createIndex(MongoDBObject("productId"->1));
    mongoClient(mongoConfig.dbName)(RATINGS_COLLECTION_NAME).createIndex(MongoDBObject("userId"->1));
    mongoClient(mongoConfig.dbName)(USERS_COLLECTION_NAME).createIndex(MongoDBObject("userId"->1));
    
    mongoClient.close();
  }
  
}