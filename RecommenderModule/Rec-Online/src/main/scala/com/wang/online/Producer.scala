package com.wang.online

import org.apache.commons.pool2.impl.GenericObjectPool
import org.apache.kafka.clients.producer.ProducerRecord

/**
  * 模拟产生数据
  **/
object Producer {
  def main(args: Array[String]): Unit = {
    // 读取配置
    val properties = PropertyUtil.properties
    // 创建生产对象
    //    val producer: KafkaProducer[String, String] = new KafkaProducer[String, String](properties)

    //写回kafka, 连接池
    val kafkaProxyPool: GenericObjectPool[KafkaProxy] = KafkaPool(PropertyUtil.getProperty("bootstrap.servers"))
    val kafkaProxy: KafkaProxy = kafkaProxyPool.borrowObject()
    var start:Int=1564754545;
    while (true) {
      //发送事件到kafka集群中
      //producer.send(new ProducerRecord[String, String](PropertyUtil.getProperty("kafka.topics"), event.toString))
      kafkaProxy.kafkaClient.send(new ProducerRecord[String, String](PropertyUtil.getProperty("test.kafka.topics"), "PRODUCT_RATING_PREFIX:112|12321|4.565|"+start.toString()))
      Thread.sleep(3000)
      start=start+1;
    }

  }
}