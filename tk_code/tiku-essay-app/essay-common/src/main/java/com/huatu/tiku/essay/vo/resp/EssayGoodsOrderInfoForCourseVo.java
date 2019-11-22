package com.huatu.tiku.essay.vo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 课程赠送批改信息
 *
 * @author geek-s
 * @date 2019-07-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EssayGoodsOrderInfoForCourseVo {

    /**
     * 扣除金额
     */
    private BigDecimal deductMoney;
}
