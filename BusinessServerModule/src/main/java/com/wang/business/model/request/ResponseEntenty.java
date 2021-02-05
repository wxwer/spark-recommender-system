package com.wang.business.model.request;


import lombok.Data;

@Data
public class ResponseEntenty <T>{
	private Integer errorCode;
	private String msg;
	private T data;
	
	public ResponseEntenty<T> success(T data) {
		this.errorCode=200;
		this.data=data;
		return this;
	}
	
	
	public ResponseEntenty<T> success(T data,String msg) {
		this.errorCode=200;
		this.data=data;
		this.msg=msg;
		return this;
	}
	public ResponseEntenty<T> fail(String msg) {
		this.errorCode=500;
		this.msg=msg;
		return this;
	}
	
}
