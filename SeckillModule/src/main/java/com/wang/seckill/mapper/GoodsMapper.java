package com.wang.seckill.mapper;

import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import com.wang.seckill.model.pojo.Goods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

public interface GoodsMapper extends BaseMapper<Goods> {

    /**
     * 不包含乐观锁
     * 根据商品Id去扣除库存 数据库操作只有一个线程去操作
     *
     * @param goods 商品
     * @return 影响行数
     */
    @Update("update goods set sale = sale - 1, gmt_modified = now() where id = #{id}")
    int updateSaleNoOptimisticLock(Goods goods);

    /**
     * 包含乐观锁
     * 根据商品Id去扣除库存 数据库操作只有一个线程去操作
     * 注意version++的时候不要在java里面，应该直接在mysql语句中写
     *
     * @param goods 商品
     * @return 影响行数
     */
    @Update("update goods set sale = sale - 1,version = version + 1, gmt_modified = now() where id = #{id} and version = #{version}")
    int updateSaleOptimisticLock(Goods goods);
    
    //根据商品id查询商品信息
    @Select("select * from goods where id = #{goodsId}")
    Goods selectGoodsById(Integer goodsId);
    
}
