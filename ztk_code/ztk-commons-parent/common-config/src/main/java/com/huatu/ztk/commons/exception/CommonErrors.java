package com.huatu.ztk.commons.exception;


/**
 * 公共错误码表
 * Created by shaojieyue
 * Created time 2016-04-27 21:28
 */
public class CommonErrors {
    public static final  ErrorResult INVALID_ARGUMENTS = ErrorResult.create(1000101,"非法的参数");
    public static final  ErrorResult SERVICE_INTERNAL_ERROR = ErrorResult.create(1000102,"服务内部错误");
    public static final  ErrorResult RESOURCE_NOT_FOUND = ErrorResult.create(1000103,"资源未发现");
    public static final  ErrorResult PERMISSION_DENIED = ErrorResult.create(1000104,"权限拒绝");

    /**
     * 用户会话过期
     */
    public static final ErrorResult SESSION_EXPIRE = ErrorResult.create(1110002,"用户会话过期");

    /**
     * 用户咋其他设备登录
     */
    public static final ErrorResult LOGIN_ON_OTHER_DEVICE = ErrorResult.create(1110004,"用户在其他设备登录");

    /**
     * 不支持收藏
     */
    public static final  ErrorResult NOT_SUPPORT_COLLECT = ErrorResult.create(1000105,"主观题不能被收藏");
    public static final  ErrorResult NOT_SUPPORT_COLLECT_KNOWLEDGE = ErrorResult.create(1000105,"知识点层级不超过三级，不能收藏");
}
