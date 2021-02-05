package com.wang.seckill.model.pojo;

import java.util.List;

import lombok.Data;

@Data
public class SessionInfoWithGoodsVo {
	//活动id
    private Integer id;
    //场次id
    private Integer sessionId;
    //开始时间
    private Long startTime;
    //结束时间
    private Long endTime;
    //属于该活动的物品列表
    private List<SessionGoodsVo> relationGoods;
}
