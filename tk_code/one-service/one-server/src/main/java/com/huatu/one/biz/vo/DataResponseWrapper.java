package com.huatu.one.biz.vo;

import lombok.Data;

import java.util.List;

/**
 * 大数据接口返回数据包装
 *
 * @author geek-s
 * @date 2019-08-29
 */
@Data
public class DataResponseWrapper<T> {

    /**
     * 课程名称
     */
    private List<T> data;

    /**
     * 状态码
     */
    private Integer code;
}
