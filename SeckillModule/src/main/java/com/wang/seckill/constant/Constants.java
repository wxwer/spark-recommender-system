package com.wang.seckill.constant;

public class Constants {
	
	//K: SESSION_CACHE_PREFIX + startTime + "_" + endTime
    //V: sessionId+"-"+skuId的List
	public static final String SESSION_CACHE_PREFIX = "seckill:sessions:";

    //K: 固定值SECKILL_CHARE_PREFIX
    //V: hash，k为sessionId+"-"+skuId，v为对应的商品信息SeckillSkuRedisTo
	public static final String SECKILL_CHARE_PREFIX = "seckill:skus";

    //K: SKU_STOCK_SEMAPHORE+商品随机码
    //V: 秒杀的库存件数
	public static final String GOODS_STOCK_SEMAPHORE = "seckill:stock:";    
	//上架商品时的分布式锁前缀
	public static final String UPLOAD_SCHEDULE_SEMAPHORE = "seckill:schedule:upload";
	
}
