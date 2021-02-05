package com.wang.seckill.enums;

import lombok.Getter;

@Getter
public enum ResultCode {
    /**
     * 响应成功
     */
    SUCCESS(true,200,"成功"),
    /**
     * 响应失败
     */
    FAILED(false,400,"错误"),

    /**
     * 响应失败，未知错误
     */
    ERROR(false,500,"未知错误");

    /**
     * 响应是否成功
     */
    private final Boolean success;
    /**
     * 响应状态码
     */
    private final Integer code;
    /**
     * 响应信息
     */
    private final String message;

    ResultCode(Boolean success, Integer code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }
}
