package com.huatu.tiku.essay.dto;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * PHP赠送批改商品
 *
 * @author geek-s
 * @date 2019-07-27
 */
@Data
public class ApiPHPCourseGoodsOrderDto {

    /**
     * 订单ID
     */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /**
     * 订单号
     */
    private String orderNum;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String userName;

    /**
     * 赠送商品
     */
    @Valid
    @NotEmpty(message = "赠送商品不能为空")
    private List<CorrectGoods> correctGoodsList;

    @Data
    public static class CorrectGoods {

        /**
         * 商品ID
         */
        @NotNull(message = "商品ID不能为空")
        private Long id;

        /**
         * 商品数量
         */
        @NotNull(message = "赠送商品数量不能为空")
        private Integer count;
    }
}
