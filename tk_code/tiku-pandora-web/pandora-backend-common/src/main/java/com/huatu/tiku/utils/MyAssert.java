package com.huatu.tiku.utils;

import com.huatu.common.exception.BizException;

import java.util.function.Supplier;

/**
 * @author zhengyi
 * @date 2018/10/24 6:14 PM
 **/
public class MyAssert {

    public static void BaseAssert(Supplier<Boolean> supplier, BizException bizException) {
        if (!supplier.get()) {
            throw bizException;
        }
    }

}