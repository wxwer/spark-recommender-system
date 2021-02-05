package com.wang.business.service;

import com.wang.business.model.domain.Product;
import com.wang.business.model.recom.Recommendation;
import com.wang.business.constant.Constant;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.util.JSON;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.rmi.server.LoaderHandler;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ProductService {

    @Autowired
    private MongoClient mongoClient;

    @Autowired
    private ObjectMapper objectMapper;

    private MongoCollection<Document> productCollection;
    private MongoCollection<Document> averageProductsScoreCollection;
    //获取Product集合
    private MongoCollection<Document> getProductCollection(){
        if(null == productCollection) {
        	productCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_PRODUCT_COLLECTION);
        }
        return productCollection;
    }
    //获取averageProductsScore集合
    private MongoCollection<Document> getAverageProductsScoreCollection(){
        if(null == averageProductsScoreCollection)
            averageProductsScoreCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_AVERAGE_PRODUCTS_SCORE_COLLECTION);
        return averageProductsScoreCollection;
    }
    //将推荐列表<物品id,score>包装成具体的推荐商品列表
    public List<Product> getRecommendProducts(List<Recommendation> recommendations){
        List<Integer> ids = new ArrayList<>();
        for (Recommendation rec: recommendations) {
            ids.add(rec.getProductId());
        }
        return getProducts(ids);
    }
    //根据商品id列表，查询具体商品信息列表
    private List<Product> getProducts(List<Integer> productIds){
        FindIterable<Document> documents = getProductCollection().find(Filters.in("productId", productIds));
        List<Product> products = new ArrayList<>();
        for (Document document: documents) {
            products.add(documentToProduct(document));
        }
        return products;
    }
    //将mongo的document对象转换为Product对象
    private Product documentToProduct(Document document){
        Product product = null;
        try{
            product = objectMapper.readValue(JSON.serialize(document), Product.class);
            Document score = getAverageProductsScoreCollection().find(Filters.eq("productId", product.getProductId())).first();
            if(null == score || score.isEmpty())
                product.setScore(0D);
            else
                product.setScore((Double) score.get("avg",0D));
        }catch (IOException e) {
            e.printStackTrace();
        }
        return product;
    }
    //根据商品id查询商品对象
    public Product findByProductId(int productId) {
        Document document = getProductCollection().find(new Document("productId", productId)).first();
        if(document == null || document.isEmpty())
            return null;
        return documentToProduct(document);
    }
    //根据商品name查询商品对象
    public List<Product> findByProductName(String name) {
        FindIterable<Document> documents = getProductCollection().find(Filters.regex("name", name));
        List<Product> products = new ArrayList<>();
        for (Document document: documents) {
            products.add(documentToProduct(document));
        }
        return products;
    }
    //将推荐列表转化为商品列表
    public List<Product> recommendationsToProducts(List<Recommendation> recommendations) {
    	if(null==recommendations || recommendations.isEmpty()) {
    		return null;
    	}
    	List<Product> products=new ArrayList<>();
    	for(Recommendation recommendation:recommendations) {
    		Integer productId=recommendation.getProductId();
    		Product product=findByProductId(productId);
    		products.add(product);
    	}
		return products;
	}
    
    

}
