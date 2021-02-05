package com.wang.business.model.domain;

import lombok.Data;

@Data
public class CommodityOrderVo {

    private Integer id;

    private Integer userId;

    private Integer goodsId;
    
    private Integer sessionId;
    
    private String orderNum;
    
    private Integer isPay;
    
    private String code;
}