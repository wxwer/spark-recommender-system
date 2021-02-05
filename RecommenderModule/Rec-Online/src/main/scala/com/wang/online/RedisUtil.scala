package com.wang.online

import redis.clients.jedis.JedisPoolConfig
import redis.clients.jedis.JedisPool
import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.MongoClientURI

object RedisUtil {
  val host="***";
  val port= 6379;
  val database= 0;
  val timeout=30000;
  val password="***";
  val config=new JedisPoolConfig;
  config.setMaxTotal(100);
  config.setMaxIdle(50);
  config.setMinIdle(10);
  //设置连接时的最大等待毫秒数
  config.setMaxWaitMillis(10000);
  //设置释放连接到池中时是否检查有效性
  config.setTestOnBorrow(true);
  config.setTestOnReturn(true);
  config.setTestWhileIdle(true);
  
  //设置每次扫描时的配置
  config.setTimeBetweenEvictionRunsMillis(30000);
  config.setNumTestsPerEvictionRun(10);
  config.setMinEvictableIdleTimeMillis(60000);
  //创建Redis连接池
  lazy val pool:JedisPool=new JedisPool(config,host,port,timeout,password,database);
  //释放连接池资源
  lazy val hook=new Thread{
    override def run(){
      pool.destroy();
    }
  };
  sys.addShutdownHook(hook);
  //创建MongoDB连接客户端
  val MONGO_URL="mongodb://127.0.0.1:27017/test";
  lazy val mongoClient=MongoClient(MongoClientURI(MONGO_URL));
  /*
  def main(args:Array[String]):Unit={
    pool.getResource.append("wang", "hello");
    val value:String=pool.getResource.get("wang");
    println(value);
  }
  */
}