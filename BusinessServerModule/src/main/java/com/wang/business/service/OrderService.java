package com.wang.business.service;

import java.util.List;

import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.google.gson.Gson;
import com.wang.business.dao.OrderMapper;
import com.wang.business.dao.SessionMapper;
import com.wang.business.model.domain.CommodityOrder;
import com.wang.business.model.domain.CommodityOrderVo;
import com.wang.business.constant.Constant;

import redis.clients.jedis.Jedis;



@Service
public class OrderService {
	
	@Autowired
	OrderMapper commodityOrderMapper;
	
	@Autowired
	SessionMapper sessionMapper;
	
	@Autowired
	Gson gson;
	
	@Autowired
	Jedis jedis;
	
	@Autowired
	RedissonClient redissonClient;
	
	//生成订单，使用事务保证原子性，如果失败需要恢复信号量
	@Transactional(rollbackFor = Exception.class)
	public boolean generateOrder(CommodityOrderVo orderVo) {
		CommodityOrder order = new CommodityOrder();
		BeanUtils.copyProperties(orderVo, order);
		try {
			sessionMapper.deductStockById(orderVo.getSessionId(),orderVo.getGoodsId());
			commodityOrderMapper.insert(order);
		} catch (Exception e) {
			if(jedis.exists(Constant.SECKILL_CHARE_PREFIX+orderVo.getSessionId()+"-"+orderVo.getGoodsId())) {
				String semaphoreKey = Constant.GOODS_STOCK_SEMAPHORE + orderVo.getSessionId() +"-"+ orderVo.getGoodsId();
				RSemaphore semaphore = redissonClient.getSemaphore(semaphoreKey);
				semaphore.release();
				jedis.del(Constant.USER_GOODS_OCCUPY+order.getUserId()+"-"+order.getGoodsId());
			}
			e.printStackTrace();
			throw e;
		}
		
		return true;
	}
	
	//查询未支付订单
	public List<CommodityOrder> findUnpayOrders(Integer userId) {
		return commodityOrderMapper.selectUnpayOrdersByUserId(userId);
	}
	//查询已支付订单
	public boolean payOrder(Integer id) {
		return commodityOrderMapper.updateOrderPayStatus(id)>0;
	}
	//取消订单，使用事务保证原子性，同时恢复信号值
	@Transactional(rollbackFor = Exception.class)
	public boolean cancelOder(Integer id) {
		try {
			CommodityOrder order = commodityOrderMapper.selectById(id);
			sessionMapper.increStockById(order.getSessionId(), order.getGoodsId());
			commodityOrderMapper.deleteOrder(id);
			if(jedis.exists(Constant.SECKILL_CHARE_PREFIX+order.getSessionId()+"-"+order.getGoodsId())) {
				String semaphoreKey = Constant.GOODS_STOCK_SEMAPHORE + order.getSessionId() +"-"+ order.getGoodsId();
				RSemaphore semaphore = redissonClient.getSemaphore(semaphoreKey);
				semaphore.release();
				jedis.del(Constant.USER_GOODS_OCCUPY+order.getUserId()+"-"+order.getGoodsId());
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return true;
	}
}
