package com.wang.kafkastream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;

public class Application {

	public static void main(String[] args) {
		String propertiesName="application.properties";
		Properties props=new Properties();
		try {
			InputStream inputStream=ClassLoader.getSystemResourceAsStream(propertiesName);
			props.load(inputStream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String brokers=props.getProperty("bootstrap.servers");
		String zookeepers = props.getProperty("zookeeper.servers");
		String from = "log";
		String to = "recommender";
		
		// 定义kafka stream 配置参数
        Properties settings = new Properties();
        settings.put(StreamsConfig.APPLICATION_ID_CONFIG, "logFilter");
        settings.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        //settings.put(StreamsConfig.ZOOKEEPER_CONNECT_CONFIG, zookeepers);
        StreamsConfig config = new StreamsConfig(settings);
        
     // 定义拓扑构建器
        Topology builder = new Topology();
        builder.addSource("SOURCE", from)
                .addProcessor("PROCESSOR", () -> new LogProcessor(), "SOURCE")
                .addSink("SINK", to, "PROCESSOR");

        // 创建kafka stream
        @SuppressWarnings("deprecation")
		KafkaStreams streams = new KafkaStreams(builder, config);

        streams.start();
        System.out.println("kafka stream started!");
		

	}

}
