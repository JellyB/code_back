package com.huatu.tiku.essay.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单明细
 *
 * @author geek-s
 * @date 2019-07-09
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminEssayCorrectGoodsVO {

    /**
     * ID
     */
    private Long id;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 价格
     */
    private Integer price;

    /**
     * 次数
     */
    private Integer num;

    /**
     * 有效期
     */
    private Integer expireDate;

    /**
     * 是否限量
     */
    private Integer isLimitNum;

    /**
     * 是否有有效期(0无有效期 1  有有效期)
     */
    private Integer expireFlag;
}