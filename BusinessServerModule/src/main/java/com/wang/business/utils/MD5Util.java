package com.wang.business.utils;

import org.springframework.util.DigestUtils;

public class MD5Util {
	private static String salt1 = "hello";
	private static String salt2 = "world";
	//对用户的密码进行编码
	public static String encode(String rawSeq) {
		StringBuilder stringBuilder=new StringBuilder();
		stringBuilder.append(salt1);
		stringBuilder.append(rawSeq);
		stringBuilder.append(salt2);
		return DigestUtils.md5DigestAsHex(stringBuilder.toString().getBytes());
	}
	//判断用户登录时的密码和数据库中保存的已加密的密码是否匹配
	public static boolean matches(String rawSeq,String encodeSeq) {
		StringBuilder stringBuilder=new StringBuilder();
		stringBuilder.append(salt1);
		stringBuilder.append(rawSeq);
		stringBuilder.append(salt2);
		String encodeString = DigestUtils.md5DigestAsHex(stringBuilder.toString().getBytes());
		return encodeString.equals(encodeSeq);
	}
}
