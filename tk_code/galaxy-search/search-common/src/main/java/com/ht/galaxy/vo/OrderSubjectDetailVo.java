package com.ht.galaxy.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author jbzm
 * @date Create on 2018/4/13 17:41
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderSubjectDetailVo {
    /**
     * 课程名称
     */
    private String subjectName;
    /**
     * 课程销量
     */
    private BigDecimal subjectSale;
    /**
     * 课程销售金额
     */
    private BigDecimal subjectMoney;
}
