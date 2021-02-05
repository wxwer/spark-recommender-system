package com.wang.business.service;

import com.wang.business.model.domain.Product;
import com.wang.business.model.recom.Recommendation;
import com.wang.business.constant.Constant;

import lombok.extern.slf4j.Slf4j;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Sorts;
import com.wang.business.model.request.*;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Service
@Slf4j
public class RecommenderService {

    @Autowired
    private MongoClient mongoClient;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private Gson gson;
    
    @Autowired
    private Jedis jedis;
    
    @Autowired
    private ProductService productService;
    
    //热榜推荐，使用redis进行缓存
    public List<Product> getHotRecommendations(HotRecommendationRequest request) {
        String today=new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    	String hotListKey = Constant.HOT_LIST_PREFIX+today;
    	String hotListProductKey = Constant.HOT_LIST_PRODUCT_PREFIX+today;
    	
    	List<Product> products = new ArrayList<>();
    	if(jedis.exists(hotListKey) && jedis.exists(hotListProductKey)) {
    		Set<Tuple> hotListSet=jedis.zrevrangeWithScores(hotListKey, 0, -1);
    		Map<String,String> hotListProductMap=jedis.hgetAll(hotListProductKey);
    		for(Tuple tuple:hotListSet) {
    			String productId=tuple.getElement();
    			Double score = tuple.getScore();
    			Product product=gson.fromJson(hotListProductMap.get(productId), Product.class);
    			product.setScore(score);
    			products.add(product);
    		}
    		return products;
    	}
    	// 获取热门电影的条目
    	MongoCollection<Document> rateMoreMoviesRecentlyCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_RATE_MORE_PRODUCTS_RECENTLY_COLLECTION);
        FindIterable<Document> documents = rateMoreMoviesRecentlyCollection.find().sort(Sorts.descending("yearmonth")).limit(request.getSum());
        List<Recommendation> recommendations = new ArrayList<>();
        Map<String, Double> sortedHotList = new HashMap<>();
        Map<String, String> hostListProduct = new HashMap<>();
        System.out.println(documents.toString());
        for (Document document : documents) {
        	Integer productId=document.getInteger("productId");
        	Double score = Double.valueOf(document.getLong("count"));
            recommendations.add(new Recommendation(productId,score ));
            sortedHotList.put(productId.toString(), score);
            
            Product product = productService.findByProductId(productId);
            product.setScore(score);
            products.add(product);
            hostListProduct.put(productId.toString(), gson.toJson(product));
        }
        jedis.zadd(hotListKey,sortedHotList);
        jedis.expire(hotListKey, 60*60*24);
        jedis.hmset(hotListProductKey, hostListProduct);
        jedis.expire(hotListProductKey, 60*60*24);
        
        //System.out.println(recommendations);
        return products;
    }
    
    // 获取评分最多电影的条目
    public List<Recommendation> getRateMoreRecommendations(RateMoreRecommendationRequest request) {
        MongoCollection<Document> rateMoreProductsCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_RATE_MORE_PRODUCTS_COLLECTION);
        FindIterable<Document> documents = rateMoreProductsCollection.find().sort(Sorts.descending("count")).limit(request.getSum());

        List<Recommendation> recommendations = new ArrayList<>();
        for (Document document : documents) {
            recommendations.add(new Recommendation(document.getInteger("productId"), 0D));
        }
        return recommendations;
    }
    
    //基于物品的协同过滤
    public List<Recommendation> getItemCFRecommendations(ItemCFRecommendationRequest request) {
        MongoCollection<Document> itemCFProductsCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_ITEMCF_COLLECTION);
        Document document = itemCFProductsCollection.find(new Document("productId", request.getId())).first();
        List<Recommendation> recommendations = new ArrayList<>();
        ArrayList<Document> recs = document.get("recs", ArrayList.class);

        for (Document recDoc : recs) {
            recommendations.add(new Recommendation(recDoc.getInteger("productId"), recDoc.getDouble("score")));
        }

        return recommendations;
    }
    
    //基于内容的推荐
    public List<Recommendation> getContentBasedRecommendations(ContentBasedRecommendationRequest request) {
        MongoCollection<Document> contentBasedProductsCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_CONTENTBASED_COLLECTION);
        
        Document document = contentBasedProductsCollection.find(new Document("productId", request.getId())).first();
        System.out.println(document.get("recs"));
        List<Recommendation> recommendations = new ArrayList<>();
        ArrayList<Document> recs = document.get("recs", ArrayList.class);

        for (Document recDoc : recs) {
            recommendations.add(new Recommendation(recDoc.getInteger("productId"), recDoc.getDouble("score")));
        }

        return recommendations;
    }

    //基于矩阵分解的推荐
    public List<Recommendation> getMatrixFactorizationRecommendations(UserRecommendationRequest request) {
        MongoCollection<Document> userRecsCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_USER_RECS_COLLECTION);
        Document document = userRecsCollection.find(new Document("userId", request.getUserId())).first();

        List<Recommendation> recommendations = new ArrayList<>();
        ArrayList<Document> recs = document.get("recs", ArrayList.class);
        for (Document recDoc : recs) {
            recommendations.add(new Recommendation(recDoc.getInteger("productId"), recDoc.getDouble("score")));
        }
        return recommendations;
    }

    // 实时推荐
    public List<Recommendation> getStreamRecommendations(UserRecommendationRequest request) {
        MongoCollection<Document> userRecsCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_STREAM_RECS_COLLECTION);
        Document document = userRecsCollection.find(new Document("userId", request.getUserId())).first();

        List<Recommendation> recommendations = new ArrayList<>();
        ArrayList<Document> recs = document.get("recs", ArrayList.class);

        for (Document recDoc : recs) {
            recommendations.add(new Recommendation(recDoc.getInteger("productId"), recDoc.getDouble("score")));
        }

        return recommendations;
    }
    
    //多路召回进行排序后综合推荐
    //如果缓存中不存在预存推荐，则同步调用推荐结果将其预存后返回，如果存在，则直接从缓存中取回部分推荐结果，如果推荐结果快被消耗完了，则异步调用并预存新的结果。
    public List<Recommendation> getMutiRetrieveRecommendations(UserRecommendationRequest request) throws Exception{
    	
    	String today=new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    	String mutiRetrieveCountKey = Constant.MUTI_RETRIEVE_COUNT_PREFIX+today;
    	String mutiRetrieveListKey = Constant.MUTI_RETRIEVE_LIST_PREFIX+today;
    	List<Recommendation> recommendations=new ArrayList<>();
    	if(jedis.exists(mutiRetrieveListKey) && jedis.exists(mutiRetrieveCountKey)) {
    		Integer count = Integer.parseInt(jedis.get(mutiRetrieveCountKey));
    		System.out.println("count:"+count);
    		if(count>Constant.MUTI_RETRIEVE_EACH_COUNT) {
    			Set<Tuple> mutiRetrieveSet = jedis.zrevrangeWithScores(mutiRetrieveListKey, Constant.MUTI_RETRIEVE_TOTAL_COUNT-count, Constant.MUTI_RETRIEVE_TOTAL_COUNT-count+Constant.MUTI_RETRIEVE_EACH_COUNT-1);
    			for(Tuple tuple:mutiRetrieveSet) {
        			String productId=tuple.getElement();
        			Double score = tuple.getScore();
        			recommendations.add(new Recommendation(Integer.parseInt(productId),score));
    			}
    			count-=Constant.MUTI_RETRIEVE_EACH_COUNT;
    			jedis.set(mutiRetrieveCountKey, Integer.toString(count));
    			return recommendations;
    		}
    		else if(count>0){
    			Set<Tuple> mutiRetrieveSet = jedis.zrevrangeWithScores(mutiRetrieveListKey, Constant.MUTI_RETRIEVE_TOTAL_COUNT-count,-1);
    			for(Tuple tuple:mutiRetrieveSet) {
        			String productId=tuple.getElement();
        			Double score = tuple.getScore();
        			recommendations.add(new Recommendation(Integer.parseInt(productId),score));
    			}
    			count=0;
    			jedis.set(mutiRetrieveCountKey, Integer.toString(count));
    			requestForNewRecommendationsAsync(request,mutiRetrieveListKey,mutiRetrieveCountKey);
    		}
    		else {
    			throw new Exception("请稍后再试");
    		}
    	}
    	else {
    		System.out.println("not exist");
    		return requestForNewRecommendations(request,mutiRetrieveListKey,mutiRetrieveCountKey);
    	}
    	if(recommendations==null || recommendations.isEmpty()) {
    		throw new Exception("请稍后再试");
    	}
    	return recommendations;
    }
    
    //从flask后端中获取经过deepFM排序后的商品
    public ResponseEntenty<String> getForEntenty(String url,HashMap<String, ?> params) throws Exception{
    	ResponseEntenty<String> responseEntenty=restTemplate.getForObject(url, ResponseEntenty.class, params);
    	return responseEntenty;
    }
    //异步更新缓存中的列表
    @Async
    private void requestForNewRecommendationsAsync(UserRecommendationRequest request,String mutiRetrieveListKey,String mutiRetrieveCountKey) {
    	requestForNewRecommendations(request, mutiRetrieveListKey, mutiRetrieveCountKey);
    }
    //更新缓存中的列表
    private List<Recommendation> requestForNewRecommendations(UserRecommendationRequest request,String mutiRetrieveListKey,String mutiRetrieveCountKey) {
    	String url=Constant.MUTI_RETRIEVE_URL;
    	HashMap<String, Integer> params=new HashMap<>();
    	List<Recommendation> recommendations=new ArrayList<>();
    	params.put("userId", request.getUserId());
    	params.put("count", Constant.MUTI_RETRIEVE_TOTAL_COUNT);
    	try {
    		ResponseEntenty responseEntenty=getForEntenty(url, params);
    		System.out.println(responseEntenty);
    		if(responseEntenty.getErrorCode()==200) {
    			Type recListType = new TypeToken<ArrayList<Recommendation>>(){}.getType();
    			recommendations=gson.fromJson((String) responseEntenty.getData(), recListType);
    			Map<String, Double> sortedRecommendations = new HashMap<>();
    			for(Recommendation recommendation:recommendations) {
    				sortedRecommendations.put(Integer.toString(recommendation.getProductId()), recommendation.getScore());
    			}
    			jedis.zadd(mutiRetrieveListKey, sortedRecommendations);
    			jedis.setex(mutiRetrieveCountKey,60*60*24, Integer.toString(Constant.MUTI_RETRIEVE_TOTAL_COUNT));
    			jedis.expire(mutiRetrieveListKey,60*60*24 );
    		}
    		else {
    			log.error(responseEntenty.getMsg());
    		}
    		
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return recommendations;
    }
}