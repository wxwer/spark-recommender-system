package com.wang.seckill.model.pojo;

import java.math.BigDecimal;

import lombok.Data;
@Data
public class SessionGoodsRedisVo {

	//活动id
	private Integer  id;
	//场次id
	private Integer sessionId;
	//商品id
	private Integer goodsId;
	//商品价格
	private BigDecimal price;
	//商品库存
	private Integer stock;
	
	//商品信息
	private Goods goods;
	//当前商品秒杀的开始时间
    private Long startTime;

    //当前商品秒杀的结束时间
    private Long endTime;

    //当前商品秒杀的随机码
    private String randomCode;
}
