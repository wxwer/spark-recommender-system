package com.wang.business.controller;

import com.wang.business.model.domain.Product;
import com.wang.business.model.recom.Recommendation;
import com.wang.business.model.request.*;
import com.wang.business.service.ProductService;
import com.wang.business.service.RatingService;
import com.wang.business.service.RecommenderService;
import com.wang.business.constant.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.extension.api.R;
import java.util.List;

@RequestMapping("/business/recommend")
@Controller
@Slf4j
public class RecommendController {
    @Autowired
    private RecommenderService recommenderService;
    @Autowired
    private ProductService productService;

    /**
     * 获取热门推荐
     *
     * @return 热门推荐商品列表
     */
    @RequestMapping(value = "/hot",  method = RequestMethod.GET)
    @ResponseBody
    public R getHotProducts(@RequestParam("num") int num) {
        try {
        	List<Product> products = recommenderService.getHotRecommendations(new HotRecommendationRequest(num));
        	return R.ok(products);
		} catch (Exception e) {
			// TODO: handle exception
			log.error(e.getLocalizedMessage());
			e.printStackTrace();
			return R.failed(e.getLocalizedMessage());
		}
    }

    /**
     * 获取打分最多的商品
     *
     * @return 近期评分最多商品列表
     */
    @RequestMapping(value = "/rate",  method = RequestMethod.GET)
    @ResponseBody
    public R getRateMoreProducts(@RequestParam("num") int num) {
        try {
        	List<Recommendation> recommendations = recommenderService.getRateMoreRecommendations(new RateMoreRecommendationRequest(num));
        	List<Product> products = productService.getRecommendProducts(recommendations);
        	return R.ok(products);
		} catch (Exception e) {
			// TODO: handle exception
			log.error(e.getLocalizedMessage());
			e.printStackTrace();
			return R.failed(e.getLocalizedMessage());
		}
    }
    
    /**
     * 基于物品的协同过滤
     * @param id 商品id
     * @return 推荐列表
     */
    @RequestMapping(value = "/itemcf/{id}", method = RequestMethod.GET)
    @ResponseBody
    public R getItemCFProducts(@PathVariable("id") int id) {
        try {
        	List<Recommendation> recommendations = recommenderService.getItemCFRecommendations(new ItemCFRecommendationRequest(id));
        	List<Product> products = productService.getRecommendProducts(recommendations);
        	return R.ok(products);
		} catch (Exception e) {
			// TODO: handle exception
			log.error(e.getLocalizedMessage());
			e.printStackTrace();
			return R.failed(e.getLocalizedMessage());
		}
        
    }
    
    /**
     * 基于内容的推荐
     * @param id 商品id
     * @return 推荐列表
     */
    @RequestMapping(value = "/contentbased/{id}",  method = RequestMethod.GET)
    @ResponseBody
    public R getContentBasedProducts(@PathVariable("id") int id) {
        try {
        	List<Recommendation> recommendations = recommenderService.getContentBasedRecommendations(new ContentBasedRecommendationRequest(id));
            List<Product> products = productService.getRecommendProducts(recommendations);
            return R.ok(products);
		} catch (Exception e) {
			// TODO: handle exception
			log.error(e.getLocalizedMessage());
			e.printStackTrace();
			return R.failed(e.getLocalizedMessage());
		}
        
        
    }
    
    /**
     * 基于矩阵分解的推荐
     * @param userId 用户id
     * @param num 召回数量
     * @return 推荐列表
     */
    @RequestMapping(value = "/offline", method = RequestMethod.GET)
    @ResponseBody
    public R getOfflineProducts(@RequestParam("userId") Integer userId, @RequestParam("num") int num) {
        
    	try {
    		//User user = userService.findByUsername(username);
            List<Recommendation> recommendations = recommenderService.getMatrixFactorizationRecommendations(new UserRecommendationRequest(userId, num));
        	List<Product> products = productService.getRecommendProducts(recommendations);
        	return R.ok(products);
		} catch (Exception e) {
			// TODO: handle exception
			log.error(e.getLocalizedMessage());
			e.printStackTrace();
			return R.failed(e.getLocalizedMessage());
		}
    }
    
    /**
     * 反映近期兴趣的实时推荐
     * @param userId 用户id
     * @param num 召回数量
     * @return 推荐列表
     */
    @RequestMapping(value = "/stream",  method = RequestMethod.GET)
    @ResponseBody
    public R getStreamProducts(@RequestParam("userId") Integer userId, @RequestParam("num") int num) {
    	try {
    		//User user = userService.findByUsername(username);
            List<Recommendation> recommendations = recommenderService.getStreamRecommendations(new UserRecommendationRequest(userId, num));
        	List<Product> products = productService.getRecommendProducts(recommendations);
        	return R.ok(products);
		} catch (Exception e) {
			// TODO: handle exception
			log.error(e.getLocalizedMessage());
			e.printStackTrace();
			return R.failed(e.getLocalizedMessage());
		}
    }
    
    /**
     * 多路召回排序后进行综合推荐
     * @param userId 用户id
     * @param num 召回数量
     * @return 推荐列表
     */
    @RequestMapping(value = "/muti",  method = RequestMethod.GET)
    @ResponseBody
    public R getMutiRetrieveProducts(@RequestParam("userId") Integer userId, @RequestParam("num") int num) {
    	try {
    		//User user = userService.findByUsername(username);
            List<Recommendation> recommendations = recommenderService.getMutiRetrieveRecommendations(new UserRecommendationRequest(userId, num));
        	List<Product> products = productService.getRecommendProducts(recommendations);
        	return R.ok(products);
		} catch (Exception e) {
			// TODO: handle exception
			log.error(e.getLocalizedMessage());
			e.printStackTrace();
			return R.failed(e.getLocalizedMessage());
		}
    }
}
