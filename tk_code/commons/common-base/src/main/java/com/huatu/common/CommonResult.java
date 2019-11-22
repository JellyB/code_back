package com.huatu.common;

/**
 * //TODO 归并success分类为SuccessResult
 * @author hanchao
 * @date 2017/12/20 9:10
 */
public class CommonResult {
    public static final Result SUCCESS = SuccessMessage.create();


    public static final ErrorResult INVALID_ARGUMENTS = ErrorResult.create(1000101,"非法的参数");
    public static final  ErrorResult SERVICE_INTERNAL_ERROR = ErrorResult.create(1000102,"服务内部错误");
    public static final  ErrorResult RESOURCE_NOT_FOUND = ErrorResult.create(1000103,"资源未发现");
    public static final  ErrorResult PERMISSION_DENIED = ErrorResult.create(1000104,"权限拒绝");
    public static final  ErrorResult FORBIDDEN = ErrorResult.create(1000105,"非法请求");


    /**
     * 未授权，用户未登录
     */
    public static final ErrorResult UNAUTHORIZED = ErrorResult.create(1110001,"用户未登录");

    /**
     * 用户会话过期
     */
    public static final ErrorResult SESSION_EXPIRE = ErrorResult.create(1110002,"用户会话过期");

    /**
     * 用户在其他设备登录
     */
    public static final ErrorResult LOGIN_ON_OTHER_DEVICE = ErrorResult.create(1110004,"用户在其他设备登录");

    /**
     * 您的账号已在其他设备登录。如非本人操作，请及时修改密码。
     */
    public static final ErrorResult LOGIN_ON_OTHER_DEVICE_RECOMMENDED_CHANGE_PASSWD = ErrorResult.create(1110005, "您的账号已在其他设备登录。如非本人操作，请及时修改密码。");
}
