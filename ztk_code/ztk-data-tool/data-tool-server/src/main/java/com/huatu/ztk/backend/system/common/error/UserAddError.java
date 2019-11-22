package com.huatu.ztk.backend.system.common.error;

import com.huatu.ztk.commons.exception.ErrorResult;

/**
 * Author: xuhuiqiang
 * Time: 11/23/16.
 */
public class UserAddError {
    public static final ErrorResult ADD_FAIL = ErrorResult.create(1100002,"用户插入失败");
    public static final ErrorResult UPDATE_FAIL = ErrorResult.create(1100003,"用户正在登录");
}
