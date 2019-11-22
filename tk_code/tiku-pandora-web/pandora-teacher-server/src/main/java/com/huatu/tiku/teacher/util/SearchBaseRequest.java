package com.huatu.tiku.teacher.util;

import lombok.Data;

import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2018/10/30
 * @描述
 */
@Data
public class SearchBaseRequest {


    /**
     * 接收json数据
     */
    private List<String> data;
    /**
     * 索引名称
     */
    private String index;
    /**
     * 索引类型
     */
    private String type;
    /**
     * 接收具体操作
     */
    private String operation;
}




