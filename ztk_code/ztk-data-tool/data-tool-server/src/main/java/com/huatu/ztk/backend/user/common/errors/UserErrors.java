package com.huatu.ztk.backend.user.common.errors;

import com.huatu.ztk.commons.exception.ErrorResult;

/**
 * Created by shaojieyue
 * Created time 2016-11-07 14:28
 */
public class UserErrors {
    public static final ErrorResult LOGIN_FAIL = ErrorResult.create(1100001,"用户名或密码错误");
}
