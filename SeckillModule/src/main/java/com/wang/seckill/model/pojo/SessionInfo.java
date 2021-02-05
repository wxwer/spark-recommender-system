package com.wang.seckill.model.pojo;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
public class SessionInfo implements Serializable {

	//活动id
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    //场次id
    private Integer sessionId;
    //商品id
    private Integer goodsId;
    //商品上架总量
    private Integer goodsStock;
    //秒杀开始时间
    private Long startTime;
    //秒杀结束时间
    private Long endTime;


}