package com.wang.seckill.model.pojo;


import lombok.Data;

@Data
public class CommodityOrderVo {
	//订单id
    private Integer id;
    //用户id
    private Integer userId;
    //商品id
    private Integer goodsId;
    //场次id
    private Integer sessionId;
    //订单编号
    private String orderNum;
    //是否付款
    private Integer isPay;
    //商品码
    private String code;
}
