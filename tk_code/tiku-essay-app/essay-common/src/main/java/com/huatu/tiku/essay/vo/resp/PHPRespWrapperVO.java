package com.huatu.tiku.essay.vo.resp;

import lombok.Data;

import java.util.List;

/**
 * PHP服务响应
 *
 * @author geek-s
 * @date 2019-07-18
 */
@Data
public class PHPRespWrapperVO<T> {

    /**
     * 数据
     */
    private List<T> data;

    /**
     * 信息
     */
    private String msg;

    /**
     * 状态码
     */
    private Integer code;
}
