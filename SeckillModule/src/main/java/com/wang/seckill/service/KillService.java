package com.wang.seckill.service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.ibatis.javassist.expr.NewArray;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.wang.seckill.constant.Constants;
import com.wang.seckill.intercepter.AuthenticationInterceptor;
import com.wang.seckill.model.pojo.CommodityOrderVo;
import com.wang.seckill.model.pojo.SessionGoodsRedisVo;
import com.wang.seckill.model.pojo.User;
import com.wang.seckill.service.KillService;

@Service
public class KillService{
	
	@Autowired
	private StringRedisTemplate redisTemplate;
	
	@Autowired
	private RedissonClient redissonClient;
	
	@Autowired
    private KafkaTemplate<String,String> kafkaTemplate;
    //kafka topic
    private String kafkaTopic="seckill";
	
    //获取当前时间内秒杀商品
	public List<SessionGoodsRedisVo> getCurrentSeckillGoods() {
        Set<String> keys = redisTemplate.keys(Constants.SESSION_CACHE_PREFIX + "*");
        Long currentTime = (Long)(System.currentTimeMillis()/1000);
        for (String key : keys) {
            String replace = key.replace(Constants.SESSION_CACHE_PREFIX, "");
            String[] split = replace.split("_");
            Long startTime = Long.parseLong(split[0]);
            Long endTime = Long.parseLong(split[1]);
            //当前秒杀活动处于有效期内
            if (currentTime > startTime && currentTime < endTime) {
                List<String> range = redisTemplate.opsForList().range(key, -100, 100);
                BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(Constants.SECKILL_CHARE_PREFIX);
                List<SessionGoodsRedisVo> collect = range.stream().map(s -> {
                    String json = (String) ops.get(s);
                    SessionGoodsRedisVo redisTo = JSON.parseObject(json, SessionGoodsRedisVo.class);
                    return redisTo;
                }).collect(Collectors.toList());
                return collect;
            }
        }
        return null;
    }
	
    //获取秒杀商品的信息
    public SessionGoodsRedisVo getSeckillGoodsInfo(Long goodsId) {
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(Constants.SECKILL_CHARE_PREFIX);
        Set<String> keys = ops.keys();
        for (String key : keys) {
            if (Pattern.matches("\\d-" + goodsId,key)) {
                String v = ops.get(key);
                SessionGoodsRedisVo redisTo = JSON.parseObject(v, SessionGoodsRedisVo.class);
                //当前商品参与秒杀活动
                if (redisTo!=null){
                    Long current = (Long)(System.currentTimeMillis()/1000);
                    //当前活动在有效期，暴露商品随机码返回
                    if (redisTo.getStartTime() < current && redisTo.getEndTime() > current) {
                        return redisTo;
                    }
                    redisTo.setRandomCode(null);
                    return redisTo;
                }
            }
        }
        return null;
    }
	
	//秒杀商品动作
	public String kill(String killId , String code,Integer num) throws Exception {
		BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(Constants.SECKILL_CHARE_PREFIX);
		String json = ops.get(killId);
		if(!StringUtils.isEmpty(json)) {
			//System.out.println("json不为空");
			SessionGoodsRedisVo redisTo = JSON.parseObject(json, SessionGoodsRedisVo.class);
			//1. 验证时效
			Long current = (Long)(System.currentTimeMillis()/1000);
			if(current>redisTo.getStartTime() && current<redisTo.getEndTime()) {
				//System.out.println("时效性满足");
				//2. 验证商品和商品随机码是否对应
				String redisKey = redisTo.getSessionId()+"-"+redisTo.getGoodsId();
				if(redisKey.equals(killId) && code.equals(redisTo.getRandomCode())) {
					//System.out.println("随机码满足");
					//3. 验证当前用户是否购买过
					User user = new User();
					user.setId(1);//AuthenticationInterceptor.userInfoLocal.get();
					Long ttl = redisTo.getEndTime() - (Long)(System.currentTimeMillis()/1000);
					String userOccupyKey = user.getId()+"-"+redisTo.getGoodsId();
					//3.1 通过在redis中使用 用户id-skuId 来占位看是否买过
					boolean occupy = redisTemplate.opsForValue().setIfAbsent(userOccupyKey,num.toString() , ttl, TimeUnit.SECONDS);
					redisTemplate.delete(userOccupyKey);
					//3.2 占位成功，说明该用户未秒杀过该商品，则继续
					if(occupy) {
						//System.out.println("用户未购买过");
						//4. 校验库存和购买量是否符合要求
						RSemaphore semaphore = redissonClient.getSemaphore(Constants.GOODS_STOCK_SEMAPHORE + code);
						//System.out.println("剩余信号量:"+semaphore.availablePermits()+" 生存时间"+semaphore.remainTimeToLive());
						//4.1 尝试获取库存信号量
						boolean acquire=false;
						try {
							acquire = semaphore.tryAcquire(num);//tryAcquire(num, 10, TimeUnit.MILLISECONDS);
						} catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
							throw e;
						}
						
						//System.out.println(acquire);
						//4.2 获取库存成功
						if(acquire) {
							//System.out.println("获取信号量成功");
							//5. 发送消息创建订单
							CommodityOrderVo order = new CommodityOrderVo();
							order.setGoodsId(redisTo.getGoodsId());
							order.setUserId(user.getId());
							order.setSessionId(redisTo.getSessionId());
							order.setIsPay(0);
							String orderNum = IdWorker.getTimeId();
							order.setOrderNum(orderNum);
							order.setCode(code);
							try {
								kafkaTemplate.send(kafkaTopic, JSON.toJSONString(order));
								//commodityOrderMapper.insert(order);
								//System.out.println("成功进入订单队列");
								return orderNum;
							} catch (Exception e) {
								// TODO: handle exception
								semaphore.release();
								redisTemplate.delete(userOccupyKey);
								e.printStackTrace();
								throw e;
							}
						}
					}
					else {
						throw new Exception("用户购买过");
					}
				}
			}
		}
		return null;
	}
}
