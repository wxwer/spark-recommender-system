package com.wang.business.model.domain;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class SessionInfo implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer sessionId;

    private Integer goodsId;

    private Integer goodsStock;
    
    private Long startTime;

    private Long endTime;


}