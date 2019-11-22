package com.huatu.ztk.backend.paperModule.bean;

import com.huatu.ztk.commons.exception.ErrorResult;

/**
 * Created by linkang on 3/3/17.
 */
public class PaperModuleErrors {


    public static final ErrorResult PAPER_MODULE_EXISTS = ErrorResult.create(30001, "该科目已存在该考试模块");
}
