package com.huatu.ztk.backend.teachType.bean;

import com.huatu.ztk.commons.exception.ErrorResult;

/**
 * Created by linkang on 3/3/17.
 */
public class TeachTypeErrors {

    public static final ErrorResult TEACH_TYPE_EXISTS = ErrorResult.create(20001, "已存在该教研题型");
}
