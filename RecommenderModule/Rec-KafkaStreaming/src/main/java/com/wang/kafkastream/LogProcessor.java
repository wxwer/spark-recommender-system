package com.wang.kafkastream;

import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;

public class LogProcessor implements Processor<byte[], byte[]>{
	private ProcessorContext context;
	@Override
	public void init(ProcessorContext context) {
		// TODO Auto-generated method stub
		this.context=context;
	}

	@Override
	public void process(byte[] key, byte[] value) {
		// TODO Auto-generated method stub
		String input = new String(value);
		if(input.contains("PRODUCT_RATING_PREFIX:")) {
			System.out.println("product rating data coming! " + input);
			input=input.split("PRODUCT_RATING_PREFIX:")[1].trim();
			context.forward("logProcessor".getBytes(), input.getBytes());
		}
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}
	
}
