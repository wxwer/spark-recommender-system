package com.wang.business.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.extension.api.R;
import com.wang.business.service.OrderService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/business/order")
@Slf4j
public class OrderController {

	@Autowired
	private OrderService orderService;
    
	/**
	 * 查询用户未支付订单
	 * @param userId 用户id
	 * @return 未支付订单列表
	 */
    @GetMapping("/unpayOrders")
    public R findUnpayOrders(@RequestParam("userId") Integer userId) {
    	try {
			return R.ok(orderService.findUnpayOrders(userId));
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return R.failed(e.getLocalizedMessage());
		}
    }
    
    /**
     * 查询用户已支付订单
	 * @param userId 用户id
	 * @return 已支付订单列表
     */
    @GetMapping("/payOrder")
    public R payOrder(@RequestParam("id") Integer id) {
    	try {
			return R.ok(orderService.payOrder(id));
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return R.failed(e.getLocalizedMessage());
		}
    }
    
    /**
     * 取消订单
     * @param id 订单id
     * @param code 商品码
     * @return 返回通知
     */
    @GetMapping("/cancelOrder")
    public R cancelOrder(@RequestParam("id") Integer id,@RequestParam("code") String code) {
    	try {
			return R.ok(orderService.cancelOder(id,code));
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return R.failed(e.getLocalizedMessage());
		}
    }
}