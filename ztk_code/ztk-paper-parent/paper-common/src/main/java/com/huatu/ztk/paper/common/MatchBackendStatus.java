package com.huatu.ztk.paper.common;

/**
 * 模考大赛管理后台状态
 * Created by linkang on 2017/09/27 下午7:41
 */
public class MatchBackendStatus {
    /**
     * 模考大赛-新建
     */
    public static final int CREATE = 1;


    /**
     * 模考大赛-审核通过
     */
    public static final int AUDIT_SUCCESS = 2;


    /**
     * 模考大赛-审核通过
     */
    public static final int AUDIT_REJECT = 3;

    /**
     * 模考大赛-已被删除
     */
    public static final int DELETE = 4;
}
