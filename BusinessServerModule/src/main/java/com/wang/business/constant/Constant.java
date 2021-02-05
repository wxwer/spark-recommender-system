package com.wang.business.constant;

public class Constant {

    //************** FOR MONGODB ****************

    public static String MONGODB_DATABASE = "test";

    public static String MONGODB_USER_COLLECTION= "User";

    public static String MONGODB_PRODUCT_COLLECTION = "Product";

    public static String MONGODB_RATING_COLLECTION = "Rating";

    public static String MONGODB_AVERAGE_PRODUCTS_SCORE_COLLECTION = "AverageProducts";

    public static String MONGODB_PRODUCT_RECS_COLLECTION = "ProductRecs";

    public static String MONGODB_RATE_MORE_PRODUCTS_COLLECTION = "RateMoreProducts";

    public static String MONGODB_RATE_MORE_PRODUCTS_RECENTLY_COLLECTION = "RateMoreRecentlyProducts";

    public static String MONGODB_STREAM_RECS_COLLECTION = "StreamRecs";

    public static String MONGODB_USER_RECS_COLLECTION = "UserRecs";

    public static String MONGODB_ITEMCF_COLLECTION = "ItemCFProductRecs";

    public static String MONGODB_CONTENTBASED_COLLECTION = "ContentBasedProductRecs";

    //************** FOR PRODUCT RATING ******************

    public static String PRODUCT_RATING_PREFIX = "PRODUCT_RATING_PREFIX";

    public static int REDIS_PRODUCT_RATING_QUEUE_SIZE = 40;
    
  //************** REDIS PREFIX ******************
    public static String HOT_LIST_PREFIX = "HotList:";
    public static String HOT_LIST_PRODUCT_PREFIX = "HotListProduct:";
    public static String RATE_MORE_PREFIX = "RateMore:";
    
  //************* MUTI RETRIEVE*******************
    public static String MUTI_RETRIEVE_URL = "http://localhost:8585/retrieve?userId={userId}&count={count}";
    public static String MUTI_RETRIEVE_COUNT_PREFIX = "MutiRetrieveCount:";
    public static String MUTI_RETRIEVE_LIST_PREFIX = "MutiRetrieveList:";
    public static int  MUTI_RETRIEVE_TOTAL_COUNT = 50;
    public static int  MUTI_RETRIEVE_EACH_COUNT = 10;
    
  
  //************* SECKILL RETRIEVE*******************
    //K: SESSION_CACHE_PREFIX + startTime + "_" + endTime
    //V: sessionId+"-"+goodsId的List
	public static final String SESSION_CACHE_PREFIX = "seckill:sessions:";

    //K: 固定值SECKILL_CHARE_PREFIX
    //V: hash，k为sessionId+"-"+goodsId，v为对应的商品信息
	public static final String SECKILL_CHARE_PREFIX = "seckill:goods";

    //K: SKU_STOCK_SEMAPHORE+商品随机码
    //V: 秒杀的库存件数
	public static final String GOODS_STOCK_SEMAPHORE = "seckill:stock:";    
	//上架商品时的分布式锁前缀
	public static final String UPLOAD_SCHEDULE_SEMAPHORE = "seckill:schedule:upload";
}
