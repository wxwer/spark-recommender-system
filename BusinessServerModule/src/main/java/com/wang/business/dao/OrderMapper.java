package com.wang.business.dao;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.business.model.domain.CommodityOrder;
@Mapper
public interface OrderMapper extends BaseMapper<CommodityOrder> {
	@Select("select * from commodity_order where user_id=#{userId} and is_pay=0")
	public List<CommodityOrder> selectUnpayOrdersByUserId(Integer userId);
	
	@Select("select * from commodity_order where user_id=#{userId} and is_pay=1")
	public List<CommodityOrder> selectPayedOrdersByUserId(Integer userId);
	
	@Select("select * from commodity_order where order_num=#{orderNum}")
	public List<CommodityOrder> selectOrderByNum(String orderNum);
	
	@Update("update commodity_order set is_pay=1 where id=#{id}")
	public int updateOrderPayStatus(Integer id);
	
	@Delete("delete from commodity_order where id=#{id}")
	public int deleteOrder(Integer id);
}