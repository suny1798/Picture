package com.suny.picture.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    SUCCESS(0, "搞定啦 (o゜▽゜)o☆"),
    PARAMS_ERROR(40000, "哎呀，参数好像填错了呢 ( >﹏< )"),
    NOT_LOGIN_ERROR(40100, "嘿，你还没登录哦 ( >﹏<。 )"),
    LOGINED_ERROR(40100, "呦，已经登录过了哦 ( >﹏<。 )"),
    NO_AUTH_ERROR(40101, "抱歉，这个操作你没有权限呢 ( >﹏<。 )"),
    NOT_FOUND_ERROR(40400, "哎呀，找不到你要的数据啦 ( >﹏<。 )"),
    FORBIDDEN_ERROR(40300, "禁止入内哦 ( >﹏<。 )"),
    SYSTEM_ERROR(50000, "系统打了个盹，稍后再试试吧 ( >﹏<。 )"),
    OPERATION_ERROR(50001, "操作失败啦，请再试一次吧 ( >﹏<。 )");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
