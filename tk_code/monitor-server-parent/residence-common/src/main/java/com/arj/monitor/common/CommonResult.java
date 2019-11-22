package com.arj.monitor.common;


import com.arj.monitor.exception.ErrorResult;
import com.arj.monitor.exception.Result;
import com.arj.monitor.exception.SuccessMessage;

/**
 * @author zhouwei
 * @Description: 异常code和msg
 * @create 2018-10-15 上午12:02
 **/
public class CommonResult {
    public static final Result SUCCESS = SuccessMessage.create();


    /**
     * 发送短息出错
     */
    public static final ErrorResult MESSAGE_SEND_ERROR = ErrorResult.create(1110010, "短信发送失败，有问题请联系客服。");


    public static final ErrorResult TELEPHONE_CODE_ERROR = ErrorResult.create(1110011, "短信验验证码有误，请重试。");

    public static final ErrorResult INVALID_ARGUMENTS = ErrorResult.create(1000101,"非法的参数");
    public static final  ErrorResult SERVICE_INTERNAL_ERROR = ErrorResult.create(1000102,"服务内部错误");
    public static final  ErrorResult RESOURCE_NOT_FOUND = ErrorResult.create(1000103,"资源未发现");
    public static final  ErrorResult PERMISSION_DENIED = ErrorResult.create(1000104,"权限拒绝");
    public static final  ErrorResult FORBIDDEN = ErrorResult.create(1000105,"非法请求");
/*      六位  用户模块 111开头     */

    /**
     * 未授权，用户未登录
     */
    public static final ErrorResult UNAUTHORIZED = ErrorResult.create(1110001,"用户未登录");
 /**
     * 账号密码错误
     */
    public static final ErrorResult USERNAME_PASSWORD_ERROR = ErrorResult.create(1110002,"账号或密码有误，请重试。");

    /**
     * 用户会话过期
     */
    public static final ErrorResult SESSION_EXPIRE = ErrorResult.create(1110003,"用户会话过期");

    /**
     * 用户咋其他设备登录
     */
    public static final ErrorResult LOGIN_ON_OTHER_DEVICE = ErrorResult.create(1110004,"用户在其他设备登录");



}
