package com.wang.seckill.redis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import com.wang.seckill.utils.RedisUtil;
import com.wang.seckill.utils.ScriptUtil;

/**
 * 执行lua脚本来保证redis执行时的原子性，在实际限流或扣库存时未使用lua脚本
 * @author Administrator
 *
 */
@Slf4j
@Component
public class RedisScript {
	//private static Integer limitValue = 200;
    private static DefaultRedisScript<List> limitScript;
    private static DefaultRedisScript<List> deductScript;
    
    @Autowired
    RedisUtil redisUtil;
    //读取lua脚本
    static {
        try {
        	limitScript = ScriptUtil.getRedisScript("./limit.lua");
        	deductScript=ScriptUtil.getRedisScript("./deductStock.lua");
        } catch (IOException e) {
            log.error("failed to read lua script",e);
            e.printStackTrace();
        }
    }
    //基于redis的技术限流
    public boolean limit(Integer limitValue) {
    	List<String> keyList = new ArrayList();
    	List<String> argList = new ArrayList();
        try {
        	RedisTemplate<String, Object> redisTemplate=redisUtil.getRedisTemplate();
            String key = String.valueOf(System.currentTimeMillis() / 1000);
            keyList.add(key);
            argList.add(String.valueOf(limitValue));
            Object result = redisTemplate.execute(limitScript, keyList, argList);
            if ((long) result != 0) {
                log.info("success to get the limit check pass");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("failed to get the limit check pass");
        return false;
    }
    //基于redis的预扣库存
    public int deductStock(Integer goodsId) {
    	List<String> keyList = new ArrayList();
    	
        try {
        	RedisTemplate<String, Object> redisTemplate=redisUtil.getRedisTemplate();
            String key = String.valueOf(goodsId);
            keyList.add(key);
            Object result = redisTemplate.execute(deductScript, keyList);
            int resInt=(int) result;
            if ( resInt>= 0) {
                log.info("success to deduct stock of goods { },version is { }",goodsId,resInt);
                return resInt;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("failed to deduct stock of goods { }",goodsId);
        return -1;
    }
}
