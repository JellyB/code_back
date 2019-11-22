package com.huatu.ztk.backend.version.error;

import com.huatu.ztk.commons.exception.ErrorResult;

/**
 * Created by linkang on 12/8/16.
 */
public class VersionErrors {
    public static final ErrorResult VERSION_TYPE_ERROR = ErrorResult.create(1315001, "版本号格式错误");

    public static final ErrorResult MD5_FAIL = ErrorResult.create(1315002, "无法计算MD5值");

    public static final ErrorResult MISSING_FILE = ErrorResult.create(1315003, "缺少升级文件");

    public static final ErrorResult WRONG_FILE_TYPE = ErrorResult.create(1315004, "文件格式错误");

    public static final ErrorResult VERSION_FILE_NOT_MATCH = ErrorResult.create(1315005, "版本与升级文件不匹配");
}
