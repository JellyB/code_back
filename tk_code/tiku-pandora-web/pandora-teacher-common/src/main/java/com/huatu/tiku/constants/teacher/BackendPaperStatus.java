package com.huatu.tiku.constants.teacher;

/**
 * 后台管理系统的试卷状态
 * Created by linkang on 2/17/17.
 */
public class BackendPaperStatus {
    /**
     * 新建状态，未审核
     */
    public static final int CREATED = 1;

    /**
     * 上线
     */
    public static final int ONLINE = 2;

    /**
     * 审核拒绝
     */
    public static final int AUDIT_REJECT = 3;

    /**
     * 删除
     */
    public static final int DELETED = 4;

    /**
     * 待审核
     */
    public static final int AUDIT_PENDING = 5;

    /**
     * 审核通过
     */
    public static final int AUDIT_SUCCESS = 6;

    /**
     * 下线
     */
    public static final int OFFLINE = 7;

    /**
     * 估分进行中
     */
    public static final int ING = 8;
    /**
     * 未上线END
     */
    public static final int BEFORE_ONLINE = 9;
    /**
     * 已上线 ，过了考试时间  已结束
     */
    public static final int END = 10;

}
