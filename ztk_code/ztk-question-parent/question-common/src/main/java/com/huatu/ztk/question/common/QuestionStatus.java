package com.huatu.ztk.question.common;


/**
 * 试题状态
 * Created by shaojieyue
 * Created time 2016-05-05 09:31
 */
public class QuestionStatus {
    /**
     * 新建状态
     */
    public static final int CREATED= 1;
    /**
     * 审核成功
     */
    public static final int AUDIT_SUCCESS= 2;
    /**
     * 审核失败
     */
    public static final int AUDIT_REJECT = 3;
    /**
     * 删除
     */
    public static final int DELETED= 4;
    /**
     * 审核成功未发布
     */
    public static final int AUDIT_SUCCESS_NOT_ISSUED= 5;
}
