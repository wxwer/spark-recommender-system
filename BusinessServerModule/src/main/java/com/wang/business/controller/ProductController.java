package com.wang.business.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.baomidou.mybatisplus.extension.api.R;
import com.wang.business.constant.Constant;
import com.wang.business.model.domain.Product;
import com.wang.business.model.request.ProductRatingRequest;
import com.wang.business.service.ProductService;
import com.wang.business.service.RatingService;

import lombok.extern.slf4j.Slf4j;


@RequestMapping("/business/product")
@Controller
@Slf4j
public class ProductController {
	
	@Autowired
    private ProductService productService;
	
	
    @Autowired
    private RatingService ratingService;
	
	
	/**
	  * 获取单个商品的信息
	 *
	 * @param id 商品id
	 * @return 商品信息
	 */
	@RequestMapping(value = "/info/{id}",method = RequestMethod.GET)
	@ResponseBody
	public R getProductInfo(@PathVariable("id") int id) {
		try {
			Product product =productService.findByProductId(id);
			return R.ok(product);
		} catch (Exception e) {
			// TODO: handle exception
			log.error(e.getLocalizedMessage());
			e.printStackTrace();
			return R.failed(e.getLocalizedMessage());
		}
	}
	
	/**
	 * 模糊查询商品
	 *
	 * @param query 查询条件
	 * @return 符合条件的商品列表
	 */
	@RequestMapping(value = "/search", method = RequestMethod.GET)
	@ResponseBody
	public R getSearchProducts(@RequestParam("query") String query) {
	    try {
	        query = new String(query.getBytes("ISO-8859-1"), "UTF-8");
	        List<Product> products = productService.findByProductName(query);
	        return R.ok(products);
	    } catch (java.io.UnsupportedEncodingException e) {
	    	log.error(e.getLocalizedMessage());
	    	e.printStackTrace();
			return R.failed(e.getLocalizedMessage());
	    }
	}
	/**
	 * 给商品评分
	 * @param id 商品id
	 * @param score 打分
	 * @param userId 用户id
	 * @return 评分确认
	 */
	@RequestMapping(value = "/rate/{id}",  method = RequestMethod.GET)
	@ResponseBody
	public R rateToProduct(@PathVariable("id") int id, @RequestParam("score") Double score, @RequestParam("userId") Integer userId) {
	    //User user = userService.findByUsername(username);
	    ProductRatingRequest request = new ProductRatingRequest(userId, id, score);
	    boolean complete = ratingService.productRating(request);
	    //埋点日志
	    if (complete) {
	        System.out.print("=========埋点=========");
	        log.info(Constant.PRODUCT_RATING_PREFIX + ":" + userId + "|" + id + "|" + request.getScore() + "|" + System.currentTimeMillis() / 1000);
	    }
	    return R.ok("已评分完成");
	}
}
