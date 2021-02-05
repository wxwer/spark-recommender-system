package com.wang.business.intercepter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.wang.business.model.domain.User;

import redis.clients.jedis.Jedis;

@Component
public class AuthenticationInterceptor implements  HandlerInterceptor {

	private Jedis jedis;
	public static ThreadLocal<User> userInfoLocal=new ThreadLocal<>();
	public AuthenticationInterceptor(Jedis jedis) {
		// TODO Auto-generated constructor stub
		this.jedis=jedis;
	}
    /**
     * 在请求处理之前进行调用（Controller方法调用之前）
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        User userInfo=null;
        Gson gson=new Gson();
    	try {
        	System.out.println("preHandle --"+request.getRequestURL());
        	String token = request.getHeader("token");
        	if(token==null) {
        		System.out.println("token is null");
        		return false;
        	}
        	String tokenKey = "token:"+token;
            if(jedis.exists(tokenKey)){
            	userInfo=gson.fromJson(jedis.get(tokenKey), User.class);
            	userInfoLocal.set(userInfo);
                return true;
            }else {
            	System.out.println("token对应的用户不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;//如果设置为false时，被请求时，拦截器执行到此处将不会继续操作
                      //如果设置为true时，请求将会继续执行后面的操作
    }
 
    /**
     * 请求处理之后进行调用，但是在视图被渲染之前（Controller方法调用之后）
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
//         System.out.println("执行了TestInterceptor的postHandle方法");
    }
 
    /**
     * 在整个请求结束之后被调用，也就是在DispatcherServlet 渲染了对应的视图之后执行（主要是用于进行资源清理工作）
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
//        System.out.println("执行了TestInterceptor的afterCompletion方法");
    }
    
}