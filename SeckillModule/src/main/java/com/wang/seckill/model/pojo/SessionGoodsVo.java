package com.wang.seckill.model.pojo;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class SessionGoodsVo {
	//活动id
	private Integer  id;
	//场次id
	private Integer sessionId;
	//商品id
	private Integer goodsId;
	//商品价格
	private BigDecimal price;
	//商品秒杀上架的总量
	private Integer stock;
}
