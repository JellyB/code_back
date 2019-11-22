package com.huatu.tiku.essay.constant.error;


import com.huatu.common.ErrorResult;

/**
 * 后台导出 相关异常
 * code格式：10006XX
 */
public class EssayExportErrors {


    /**
     * 生成文件类型错误
     */
    public static final ErrorResult ERROR_FILE_TYPE = ErrorResult.create(1000601, "文件类型错误");

    /**
     * 数据列表
     */
    public static final ErrorResult EMPTY_LIST = ErrorResult.create(1000602, "所选列表为空");
    /**
     * 生成文件类型错误
     */
    public static final ErrorResult ERROR_TYPE = ErrorResult.create(1000603, "导出范围选择错误");

    /**
     * 文件下载资源未发现
     */
    public static final ErrorResult RESOURCE_NOT_FOUND = ErrorResult.create(1000103, "资源未发现");


}
