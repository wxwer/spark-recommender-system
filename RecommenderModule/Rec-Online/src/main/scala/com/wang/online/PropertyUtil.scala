package com.wang.online

import java.util.Properties
import java.io.InputStream

object PropertyUtil {
  val propertiesName="application.properties";
  val properties=new Properties();
  //加载属性配置文件
  try{
    val inputStream:InputStream=ClassLoader.getSystemResourceAsStream(propertiesName);
    properties.load(inputStream);
  } catch {
    case t: Throwable => t.printStackTrace();
  };
  //获取属性值
  def getProperty(key:String)=properties.getProperty(key);
  /*
  def main(args:Array[String]):Unit={
    println(getProperty("bootstrap.servers"));
  }
  */
}