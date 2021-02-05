package com.wang.seckill.model.pojo;


import java.io.Serializable;
import lombok.Data;

@Data
public class User implements Serializable {
	//用户id
    private Integer id;
    //用户昵称
    private String name;
    //用户账户名
    private String username;
    //用户密码
    private String password;
    //注册时间
    private Long timestamp;
    //携带的token
    private String token;
}
