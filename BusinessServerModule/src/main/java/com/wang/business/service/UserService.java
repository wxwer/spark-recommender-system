package com.wang.business.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.wang.business.dao.UserMapper;
import com.wang.business.model.domain.User;
import com.wang.business.model.request.LoginUserRequest;
import com.wang.business.model.request.RegisterUserRequest;
import com.wang.business.utils.MD5Util;

@Service
public class UserService {

	@Autowired
	private UserMapper userMapper;
	
	@Autowired
	private Gson gson;
	
	@Autowired
	private Jedis jedis;
	//用户注册
    public boolean registerUser(RegisterUserRequest request){
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword( MD5Util.encode(request.getPassword()));
        user.setName(request.getName());
        user.setTimestamp(System.currentTimeMillis());
        try{
            userMapper.insertUser(user);
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
		return false;
    }
    //用户登录
    public User loginUser(LoginUserRequest request){
        User user = userMapper.findByUsername(request.getUsername());
        if(null == user) {
            return null;
        }else if(!MD5Util.matches(request.getPassword(), user.getPassword())){
            return null;
        }
        user.setPassword("");
        String token = generateToken(user);
        boolean isSaved = saveTokenInRedis(user, token);
        user.setToken(token);
        return isSaved?user:null;
    }
    //用户注销
    public boolean logoutUser(String username) {
    	String tokenKey1 = "token:"+username;
    	
    	try {
			if(!jedis.exists(tokenKey1)) {
				return true;
			}else{
				String token = jedis.get(tokenKey1);
				String tokenKey2 = "token:"+token;
				jedis.del(tokenKey1);
				jedis.del(tokenKey2);
				return true;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
    	return false;
    }
    //查询用户是否登录
    public boolean isUserLogin(String username) {
    	String tokenKey1 = "token:"+username;
		if(jedis.exists(tokenKey1)) {
			String token = jedis.get(tokenKey1);
			String tokenKey2 = "token:"+token;
			if(jedis.exists(tokenKey2)) {
				return true;
			}
		}
		return false;
	}
    //更新用户的昵称
    public boolean updateUser(User user){
        return userMapper.updateName(user)>0;
    }
    //为每位登录的用户生成随机的token
    private String generateToken(User user) {
    	String username=user.getUsername();
    	String dateSalt = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    	String numSalt = Integer.toString(new Random().nextInt(10000));
    	String tokenString = username+dateSalt+numSalt;
    	return MD5Util.encode(tokenString);
    }
    //将token保存到redis中，需要保证登录的唯一性，同时设置过期时间
    private boolean saveTokenInRedis(User user,String token) {
    	String tokenKey1 = "token:"+user.getUsername();
    	String tokenKey2 = "token:"+token;
    	try {
    		if(jedis.exists(tokenKey1)) {
    			String token_tmp = jedis.get(tokenKey1);
				String tokenKey2_tmp = "token:"+token_tmp;
				jedis.del(tokenKey2_tmp);
    		}
			jedis.setex(tokenKey1, 60*60*10, token);
			
			jedis.setex(tokenKey2,60*60*10, gson.toJson(user));
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return false;
	}
}
