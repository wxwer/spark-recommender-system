package com.wang.business.model.request;

import lombok.Data;

@Data
public class RegisterUserRequest {

	private String name;
	
    private String username;

    private String password;

}
