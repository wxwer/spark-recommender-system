package com.wang.seckill.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.seckill.model.pojo.SessionInfo;

public interface SessionMapper extends BaseMapper<SessionInfo>{
	@Select("select * from session_info where start_time<#{current} and end_time>#{current}")
	List<SessionInfo> findSessionsByTime(Long current);
	
	@Update("update session_info set goods_stock=#{stock} where session_id=#{sessionId} and goods_id=#{goodsId}")
	int updateStockById(Integer stock, Integer sessionId,Integer goodsId);
	
	@Update("update session_info set goods_stock=goods_stock-1 where session_id=#{sessionId} and goods_id=#{goodsId}")
	int deductStockById(Integer sessionId,Integer goodsId);
	
	@Update("update session_info set goods_stock=goods_stock+1 where session_id=#{sessionId} and goods_id=#{goodsId}")
	int increStockById(Integer sessionId,Integer goodsId);
}
