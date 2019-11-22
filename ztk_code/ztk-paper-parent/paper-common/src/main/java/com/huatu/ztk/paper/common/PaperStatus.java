package com.huatu.ztk.paper.common;


/**
 * 试卷状态
 * Created by shaojieyue
 * Created time 2016-05-05 09:31
 */
public class PaperStatus {
    /**
     * 新建状态 模块估分:未发布
     */
    public static final int CREATED= 1;

    /**
     * 审核成功 模块估分:已发布
     */
    public static final int AUDIT_SUCCESS= 2;

    /**
     * 审核拒绝
     */
    public static final int AUDIT_REJECT = 3;

    /**
     * 删除 模块估分:已下线
     */
    public static final int DELETED= 4;
}
