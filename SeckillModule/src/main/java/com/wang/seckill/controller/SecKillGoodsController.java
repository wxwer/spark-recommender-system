package com.wang.seckill.controller;


import java.util.List;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.extension.api.R;
import com.wang.seckill.model.pojo.SessionGoodsRedisVo;
import com.wang.seckill.model.pojo.SessionInfoWithGoodsVo;
import com.wang.seckill.service.GoodsService;
import com.wang.seckill.service.KillService;

@RestController
@RequestMapping("/seckill/goods")
public class SecKillGoodsController {
	@Autowired
	GoodsService goodsService;
	
	@Autowired
	KillService killService;
	
	@Autowired
	RedissonClient redissonClient;
	/**
	 * 获取秒杀的场次-商品信息
	 * @return
	 */
	@GetMapping("/sessions")
	public R getKillSessions() {
		try {
			List<SessionInfoWithGoodsVo> sessions = goodsService.getSeckillSessions();
			return R.ok(sessions);
		} catch (Exception e) {
			e.printStackTrace();
			return R.failed(e.getLocalizedMessage());
		}
	}
	/**
	 * 上架秒杀商品
	 * @return
	 */
	@GetMapping("/uploadSeckillGoods")
	public R uploadSeckillGoodsLatest() {
		try {
			goodsService.uploadSeckillGoodsLatest();
			return R.ok("已上架近期秒杀场次商品");
		} catch (Exception e) {
			e.printStackTrace();
			return R.failed(e.getLocalizedMessage());
		}
	}
	/**
	 * 获取当前已上架并在有效期内的秒杀商品
	 * @return
	 */
	@GetMapping("/currentSeckillGoods")
	public R currentSeckillGoods() {
		try {
			List<SessionGoodsRedisVo> currentSeckillGoods = killService.getCurrentSeckillGoods();
			return R.ok(currentSeckillGoods);
		} catch (Exception e) {
			e.printStackTrace();
			return R.failed(e.getLocalizedMessage());
		}
	}
	/**
	 * 根据商品id获取秒杀商品信息
	 * @param sessionId 场次id
	 * @param goodsId 商品id
	 * @return 商品信息
	 */
	@GetMapping("/seckillGoodsInfo")
	public R getSeckillGoodsInfo(@RequestParam("sessionId") Integer sessionId,@RequestParam("goodsId") Integer goodsId) {
		try {
			SessionGoodsRedisVo sessionGoods = killService.getSeckillGoodsInfo(sessionId,goodsId);
			return R.ok(sessionGoods);
		} catch (Exception e) {
			e.printStackTrace();
			return R.failed(e.getLocalizedMessage());
		}
	}
}

