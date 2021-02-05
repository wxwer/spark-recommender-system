package com.wang.business.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wang.business.intercepter.AuthenticationInterceptor;
import com.wang.business.model.domain.User;
import com.wang.business.model.request.LoginUserRequest;
import com.wang.business.model.request.RegisterUserRequest;
import com.wang.business.model.request.ResponseEntenty;
import com.wang.business.service.UserService;

import lombok.extern.slf4j.Slf4j;

@RequestMapping("/business/user")
@Controller
@Slf4j
public class UserController{
	@Autowired
	private UserService userService;
	
	/**
	 * 用户注册
	 * @param request 请求体，包含用户账户名和密码
	 * @return 注册结果
	 */
	@RequestMapping(value = "/register",  method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntenty<String> userRegister(@RequestBody  RegisterUserRequest request){

		boolean isRegister=userService.registerUser(request);
		if(isRegister) {
			return new ResponseEntenty<String>().success("","success");
		}
		else {
			return new ResponseEntenty<String>().fail("注册失败，用户已存在");
		}
    }
	
	/**
	 * 用户登录
	 * @param request 请求题，包含账户名和密码
	 * @return 登录结果
	 */
	@RequestMapping(value = "/login",  method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntenty<User> userLogin(@RequestBody  LoginUserRequest request){

		User user=userService.loginUser(request);
		if(user!=null) {
			return new ResponseEntenty<User>().success(user,"success");
		}
		else {
			return new ResponseEntenty<User>().fail("登录失败，用户名或密码错误");
		}
    }
	
	/**
	 * 用户注销
	 * @param username 用户账号
	 * @return 注销结果
	 */
	@RequestMapping(value = "/logout",  method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntenty<String> userLogout(@RequestParam("username") String username){
		try {
			boolean isLogout = userService.logoutUser(username);
			if(isLogout) {
				return new ResponseEntenty<String>().success("",username+"注销成功");
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return new ResponseEntenty<String>().fail(username+"注销失败");
    }
    
	/**
	 * 查询用户是否登录
	 * @param username 用户账号
	 * @return 登录结果
	 */
	@RequestMapping(value = "/islogin",  method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntenty<String> isUserLogin(@RequestParam("username") String username){
		//System.out.println("===>"+AuthenticationInterceptor.userInfoLocal.get());
		try {
			boolean isLogin = userService.isUserLogin(username);
			if(isLogin) {
				return new ResponseEntenty<String>().success("",username+"已登录");
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return new ResponseEntenty<String>().fail("未登录或登录已过期");
    }
}
