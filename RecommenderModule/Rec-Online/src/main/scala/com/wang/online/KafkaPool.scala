package com.wang.online

import java.util.Properties
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.commons.pool2.BasePooledObjectFactory
import org.apache.commons.pool2.PooledObject
import org.apache.commons.pool2.impl.DefaultPooledObject
import org.apache.commons.pool2.impl.GenericObjectPool

//包装kafka客户端
class KafkaProxy(broker:String){
  val props:Properties=new Properties();
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName);
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName);
  
  val kafkaClient=new KafkaProducer[String,String](props);
}

//创建一个kafka工厂
class KafkaProxyFactory(broker:String) extends BasePooledObjectFactory[KafkaProxy]{
  override def create():KafkaProxy=new KafkaProxy(broker);
  override def wrap(t:KafkaProxy):PooledObject[KafkaProxy]=new DefaultPooledObject[KafkaProxy](t);
}
//通过kafka工厂，创建kafka连接池对象
object KafkaPool {
  private var kafkaPool:GenericObjectPool[KafkaProxy]=null;
  
  def apply(broker:String):GenericObjectPool[KafkaProxy]={
    if(kafkaPool==null){
      KafkaPool.synchronized{
        this.kafkaPool=new GenericObjectPool[KafkaProxy](new KafkaProxyFactory(broker));
      }
    }
    kafkaPool;
  }
}