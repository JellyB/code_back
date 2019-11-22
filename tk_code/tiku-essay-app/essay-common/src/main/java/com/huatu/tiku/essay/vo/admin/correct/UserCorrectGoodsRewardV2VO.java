package com.huatu.tiku.essay.vo.admin.correct;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author zhaoxi
 * @Description: 赠送批改次数
 * @date 2018/12/284:13 PM
 */
@Data
public class UserCorrectGoodsRewardV2VO {

    /**
     * 批量上代报文件地址
     */
    private String url;

    /**
     * 账号信息列表
     */
    @NotNull(message = "用户名或手机号")
    private List<String> accountList;

    /**
     * 实际支付
     */
    @NotBlank(message = "实收金额不能为空")
    private Double realMoney;

    /**
     * 赠送商品
     */
    @Valid
    @NotEmpty(message = "赠送商品不能为空")
    private List<UserCorrectGoodsRewardV2VO.CorrectGoods> correctGoodsList;

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

    /**
     * 操作人
     */
    private String creator;

    /**
     * 备注
     */
    private String remark;
}
