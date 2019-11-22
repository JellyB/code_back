package com.huatu.ztk.backend.paper.error;

import com.huatu.ztk.commons.exception.ErrorResult;

/**
 * @author Created by jbzm on 180101.
 */
public class EssayErrors {

    /**
     * 上传失败
     */
    public static final ErrorResult NO_ESSAY_INFO = ErrorResult.create(1116001, "pc课程课程信息没有添加");
    public static final ErrorResult NO_ESSAY_PRACTICE = ErrorResult.create(1116002, "申论相关信息错误");
    public static final ErrorResult ESSAY_ID_ERROR = ErrorResult.create(1116003, "申论ID不对");
    public static final ErrorResult ESSAY_TIME_ERROR = ErrorResult.create(1116004, "答题时间小于申论时间");

}
