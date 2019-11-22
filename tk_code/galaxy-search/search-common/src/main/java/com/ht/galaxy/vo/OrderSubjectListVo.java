package com.ht.galaxy.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author jbzm
 * @date Create on 2018/4/13 16:41
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderSubjectListVo {
    /**
     * 时间
     */
    private String date;
    /**
     * 课程类型
     */
    private List<OrderSubjectTypeVo> orderSubjectTypeVos;
}
