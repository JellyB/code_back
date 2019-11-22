package com.huatu.one.biz.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 选项
 *
 * @author geek-s
 * @date 2019-09-12
 */
@Data
@Builder
public class OptionVo {

    /**
     * ID
     */
    private String value;

    /**
     * 业绩类型
     */
    private String text;
}
