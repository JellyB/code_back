package com.ht.galaxy.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author jbzm
 * @date Create on 2018/4/13 17:37
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderSubjectTypeVo {
    /**
     * 课程详细
     */
    private List<OrderSubjectDetailVo> orderSubjectDetailVo;
    /**
     * 课程类型
     */
    private String subjectType;
}
