package com.wang.online

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.DataFrame
import org.apache.spark.rdd.RDD
import org.apache.spark.SparkContext
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.Seconds
import org.apache.spark.broadcast.Broadcast
//import scala.collection.immutable.Map
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka.KafkaUtils
import kafka.serializer.StringDecoder
import org.apache.spark.streaming.dstream.DStream


object OnlineRecommender {
  val STREAM_RECS="StreamRecs";
  val PRODUCT_RECS = "ProductRecs";
  
  val MAX_USER_RATING_NUM = 20;
  val MAX_SIM_PRODUCTS_NUM = 20;
  
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
    
    //定义SparkContext
    val sparkContext:SparkContext=sparkSession.sparkContext;
    //定义Spark-Streaming Context
    val streamingContext:StreamingContext=new StreamingContext(sparkContext,Seconds(5));
    
    val productRecs = OperateMongoDb.loadInfoAsDataFrame(sparkSession, PRODUCT_RECS).as[RecommendationProduct].rdd
      .map{
        x=>
          (x.productId,x.recs.map(x=>(x.productId,x.score)).toMap);
    }.collectAsMap();
    //定义广播变量
    val productBroadcast:Broadcast[collection.Map[Int,Map[Int,Double]]] = sparkContext.broadcast(productRecs);
    println(productBroadcast.value);
    /***************************************************************/
    //kafka的消费者配置
    val kafkaPro:Map[String, String] = Map[String, String](
      //用于初始化链接到集群的地址
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> PropertyUtil.getProperty("bootstrap.servers"),
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer].getName,
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer].getName,
      //用于标识这个消费者属于哪个消费团体
      ConsumerConfig.GROUP_ID_CONFIG -> PropertyUtil.getProperty("product.group.id"),
      //如果没有初始化偏移量或者当前的偏移量不存在任何服务器上，可以使用这个配置属性  112|12321|4.565|1564754545
      ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "largest"
    );
    //产生消费者数据流
    val stream:InputDStream[(String,String)]=KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](streamingContext, kafkaPro, Set(PropertyUtil.getProperty("product.kafka.topics")));
    //对kafka stream进行处理，产生评分流，userId|productId|score|timestamp
    val ratingStream:DStream[(Int,Int,Double,Int)] = stream.map{
      msg =>
        val item=msg._2.split("\\|");
        (item(0).toInt,item(1).toInt,item(2).toDouble,item(3).toInt);
    };
    ratingStream.foreachRDD{ rdd =>
      rdd.map{
        case (userId, productId, score, timestamp) =>
          println(s"==================>$userId+$productId+$score+$timestamp");
          // 获取当前用户最近的M次商品评分 uid mid:score mid:score mid:score  =========>(mid,score)
        val userRecentlyRatings: Array[(Int, Double)] = Commons.getUserRecentlyRating(MAX_USER_RATING_NUM, userId);

        // 获取商品P最相似的且当前用户未看过的K个商品
        val simMovies: Array[Int] = Commons.getTopSimProducts(MAX_SIM_PRODUCTS_NUM, productId, userId, productBroadcast.value);

        // 计算待选商品的推荐优先级
        val streamRecs = Commons.computeProductScores(productBroadcast.value, userRecentlyRatings, simMovies);
        //将数据保存到MongoDB
        OperateMongoDb.saveRecsToMongoDB(userId, streamRecs,STREAM_RECS);
        
      }.count();
      
    };
    streamingContext.start();
    streamingContext.awaitTermination();
  }
}