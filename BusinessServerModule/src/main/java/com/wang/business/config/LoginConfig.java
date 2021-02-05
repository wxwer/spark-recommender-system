package com.wang.business.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.wang.business.intercepter.AuthenticationInterceptor;

import redis.clients.jedis.Jedis;

@Configuration
public class LoginConfig implements WebMvcConfigurer {
    @Autowired
    private Jedis jedis;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration registration = registry.addInterceptor(new AuthenticationInterceptor(jedis));
        registration.addPathPatterns("/**");     //需要拦截的路径   
        registration.excludePathPatterns("/business/user/register",
        								 "/business/user/login");      //不需要拦截的路径
    }
}