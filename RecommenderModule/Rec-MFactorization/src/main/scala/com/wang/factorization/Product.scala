package com.wang.factorization


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
  * 评分数据集
  *
  * @param userId    用户id
  * @param productId 商品id
  * @param score     评分数
  * @param timestamp 时间
  */
case class ProductRating(userId: Int, productId: Int, score: Double,timestamp:Int);

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
  * 商品相似推荐
  *
  * @param productId 商品id
  * @param recs      相似的商品集合
  */
case class RecommendationProduct(productId: Int, recs: Seq[Recommendation]);

/**
  * 用户推荐
  *
  * @param userId 用户id
  * @param recs      推荐的商品集合
  */
case class RecommendationUser(userId: Int, recs: Seq[Recommendation]);