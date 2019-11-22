package com.huatu.one.biz.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 默认选项
 *
 * @author geek-s
 * @date 2019-09-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DefaultOptionVo {

    /**
     * 开始时间
     */
    private String dateBegin;

    /**
     * 结束时间
     */
    private String dateEnd;

    /**
     * 默认考试类型
     */
    private Long examTypeId;
}
