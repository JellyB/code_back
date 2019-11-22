package com.huatu.ztk.search.bean;

import com.huatu.ztk.commons.exception.ErrorResult;

/**
 * Created by renwenlong on 2016/9/9.
 */
public class SearchErrors {

    /**
     * 搜索关键字为空
     */
    public static final ErrorResult KEYWORD_EMPTY = ErrorResult.create(1120001, "搜索关键字为空");

    /**
     * 搜索关键字过长
     */
    public static final ErrorResult KEYWORD_TOO_LONG = ErrorResult.create(1120002, "搜索关键字过长");

}
