package com.wang.business.kafka;

import java.util.Optional;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import com.google.gson.Gson;
import com.wang.business.model.domain.CommodityOrderVo;
import com.wang.business.service.OrderService;


// kafka消费者，负责消费消息，并生成订单


@Component
@Slf4j
public class KafkaConsumer {
	private String kafkaTopic="seckill";
	
	@Autowired
	private Gson gson;
	
	@Autowired
	private OrderService orderService;
	
	@KafkaListener(topics = {"seckill"})
	public void listen(ConsumerRecord<String, String> record) throws Exception{
		Optional<?> kafkaMessage = Optional.ofNullable(record.value());
		if(kafkaMessage.isPresent()) {
			String message = (String) kafkaMessage.get();
			CommodityOrderVo orderVo=gson.fromJson(message, CommodityOrderVo.class);
			try {
				orderService.generateOrder(orderVo);
				log.info("成功生成订单，userId={},goodsId={}",orderVo.getUserId(), orderVo.getGoodsId());
			} catch (Exception e) {
				log.error(e.getLocalizedMessage());
				log.info("生成订单时出现错误，userId={},goodsId={}",orderVo.getUserId(), orderVo.getGoodsId());
			}
		}
		else {
			log.warn("消息不存在");
		}
		
	}
}