package com.wang.seckill.schedule;


import java.util.concurrent.TimeUnit;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.wang.seckill.constant.Constants;
import com.wang.seckill.service.GoodsService;
import lombok.extern.slf4j.Slf4j;
/*
 * 上架秒杀商品的定时任务
 */
@Component
@Slf4j
public class SecKillSchedule {
	@Autowired
	RedissonClient redissonClient;
	
	@Autowired
	GoodsService goodsService;
	
	@Async
	@Scheduled(cron = "0 0 3 * * ?")
	//每天晚上三点定时上架最近场次的秒杀商品
	public void uploadSeckillGoodsLatest() {
		RLock lock = redissonClient.getLock(Constants.UPLOAD_SCHEDULE_SEMAPHORE);
		boolean acquire = false;
		try {
			acquire = lock.tryLock(100, 1000, TimeUnit.MILLISECONDS);
			if(acquire) {
				goodsService.uploadSeckillGoodsLatest();
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
		}finally {
			if(acquire) {
				lock.unlock();
			}
		}
	}
}
