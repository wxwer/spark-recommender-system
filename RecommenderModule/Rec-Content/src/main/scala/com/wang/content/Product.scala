package com.wang.content

/**
  * 商品数据集
  *
  * @param productId  商品id
  * @param name       商品名
  * @param imageUrl   地址
  * @param categories 商品分类
  * @param tags       商品标签
  */
case class Product(productId: Int, name: String, imageUrl: String, categories: String, tags: String);

/**
  * MongoDB 配置对象
  *
  * @param url    地址
  * @param dbName 数据库名
  */
case class MongoConfig(val url: String, val dbName: String);

/**
  * 推荐对象
  *
  * @param productId 商品id
  * @param score     推荐度 评分
  */
case class Recommendation(productId: Int, score: Double);

/**
  * 电影相似推荐
  *
  * @param productId 商品id
  * @param recs      相似的电影集合
  */
case class RecommendationProduct(productId: Int, recs: Seq[Recommendation]);
