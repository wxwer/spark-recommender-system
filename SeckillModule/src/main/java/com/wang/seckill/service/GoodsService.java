package com.wang.seckill.service;

import com.wang.seckill.model.pojo.Goods;
import com.wang.seckill.model.pojo.SessionGoodsRedisVo;
import com.wang.seckill.model.pojo.SessionGoodsVo;
import com.wang.seckill.model.pojo.SessionInfo;
import com.wang.seckill.model.pojo.SessionInfoWithGoodsVo;
import com.wang.seckill.constant.Constants;
import com.wang.seckill.mapper.GoodsMapper;
import com.wang.seckill.mapper.SessionMapper;
import com.wang.seckill.service.GoodsService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
public class GoodsService extends ServiceImpl<GoodsMapper, Goods>{
	@Autowired
	private SessionMapper sessionMapper;
	
	@Autowired
	private GoodsMapper goodsMapper;
	
	@Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

	//上架最近场次的秒杀商品到redis，进行库存预热
	public void uploadSeckillGoodsLatest() {
		List<SessionInfoWithGoodsVo> sessions = getSeckillSessions();
		saveSecKillSession(sessions);
		saveSecKillGoods(sessions);
	}
	
	//获得所有符合时间条件的场次信息
	public List<SessionInfoWithGoodsVo> getSeckillSessions() {
		Long current = (Long)(System.currentTimeMillis()/1000);
		List<SessionInfo> sessionInfos = sessionMapper.findSessionsByTime(current);
		
		List<SessionInfoWithGoodsVo> sessions = new ArrayList<>();
		Map<Integer, SessionInfoWithGoodsVo> sessionMaps =new HashMap<>();
		
		for(SessionInfo sessionInfo:sessionInfos) {
			Integer sessionId = sessionInfo.getSessionId();
			SessionInfoWithGoodsVo sessionInfoWithGoodsVo=new SessionInfoWithGoodsVo();
			BeanUtils.copyProperties(sessionInfo, sessionInfoWithGoodsVo);
			Goods goods = goodsMapper.selectGoodsById(sessionInfo.getGoodsId());
			SessionGoodsVo sessionGoodsVo  = new SessionGoodsVo();
			BeanUtils.copyProperties(goods, sessionGoodsVo);
			sessionGoodsVo.setId(sessionInfo.getId());
			sessionGoodsVo.setSessionId(sessionId);
			sessionGoodsVo.setStock(sessionInfo.getGoodsStock());
			sessionGoodsVo.setGoodsId(goods.getId());
			
			if(!sessionMaps.containsKey(sessionId)) {
				sessionInfoWithGoodsVo.setRelationGoods(new ArrayList<>());
				sessionMaps.put(sessionId, sessionInfoWithGoodsVo);
			}
			sessionMaps.get(sessionId).getRelationGoods().add(sessionGoodsVo);
		}
		for(SessionInfoWithGoodsVo sessionInfoWithGoodsVo:sessionMaps.values()) {
			sessions.add(sessionInfoWithGoodsVo);
		}
		return sessions;
	}
	
	//保存秒杀商品的场次信息到redis
	private void saveSecKillSession(List<SessionInfoWithGoodsVo> sessions) {
        sessions.stream().forEach(session->{
            String key = Constants.SESSION_CACHE_PREFIX + session.getStartTime() + "_" + session.getEndTime();
            //当前活动信息未保存过
            if(redisTemplate.hasKey(key)) {
            	redisTemplate.delete(key);
            }
            List<String> values = session.getRelationGoods().stream()
                    .map(sku -> sku.getSessionId() +"-"+ sku.getGoodsId())
                    .collect(Collectors.toList());
            redisTemplate.opsForList().leftPushAll(key,values);
        });
    }
	//保存秒杀商品信息到redis，并生成商品随机码
    private void saveSecKillGoods(List<SessionInfoWithGoodsVo> sessions) {
        BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(Constants.SECKILL_CHARE_PREFIX);
        sessions.stream().forEach(session->{
            session.getRelationGoods().stream().forEach(sku->{
                String key = sku.getSessionId() +"-"+ sku.getGoodsId();
                if(redisTemplate.hasKey(key)) {
                	redisTemplate.delete(key);
                }
                SessionGoodsRedisVo redisTo = new SessionGoodsRedisVo();
                //1. 保存SeckillSkuVo信息
                BeanUtils.copyProperties(sku,redisTo);
                //2. 保存开始结束时间
                redisTo.setStartTime(session.getStartTime());
                redisTo.setEndTime(session.getEndTime());
                //3. 远程查询sku信息并保存
                Goods goods = goodsMapper.selectGoodsById(sku.getGoodsId());
                redisTo.setGoods(goods);
                //4. 生成商品随机码，防止恶意攻击
                String token = DigestUtils.md5DigestAsHex((UUID.randomUUID().toString().replace("-", "")+System.currentTimeMillis()).getBytes());
                redisTo.setRandomCode(token);
                //5. 序列化为json并保存
                String jsonString = JSON.toJSONString(redisTo);
                ops.put(key,jsonString);
                //5. 使用库存作为Redisson信号量限制库存
                RSemaphore semaphore = redissonClient.getSemaphore(Constants.GOODS_STOCK_SEMAPHORE + token);
                semaphore.expire(60*30, TimeUnit.SECONDS);
                semaphore.trySetPermits(sku.getStock());
            });
        });
    }
	
	
}
