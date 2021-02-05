package com.wang.business.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.wang.business.model.domain.User;

@Mapper
public interface UserMapper {
	@Insert("insert into user(name,username,password,timestamp) values(#{name},#{username},#{password},#{timestamp})")
	int insertUser(User user);
	
	@Select("select * from user where username=#{username}")
	User findByUsername(@Param("username") String username);
	
	@Update("update user set name=#{name},timestamp=#{timestamp} where username=#{username}")
	int updateName(User user);
	
}
