package com.wang.seckill.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.wang.seckill.intercepter.AuthenticationInterceptor;

/*
 * 登录拦截的配置
 */
@Configuration
public class LoginConfig implements WebMvcConfigurer {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration registration = registry.addInterceptor(new AuthenticationInterceptor(redisTemplate));
        //registration.addPathPatterns("/**");     //需要拦截的路径  
        registration.excludePathPatterns("/**");
    }
}
