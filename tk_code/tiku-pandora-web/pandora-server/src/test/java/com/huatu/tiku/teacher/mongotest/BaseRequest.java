package com.huatu.tiku.teacher.mongotest;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author jbzm
 * @date 2018/7/18 9:06 PM
 **/
@Getter
@Setter
public class BaseRequest {
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