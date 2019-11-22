package com.huatu.tiku.essay.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 商品订单退款记录
 *
 * @author geek-s
 * @date 2019-07-08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminEssayGoodsOrderRefundRecordVO {

    /**
     * 操作时间
     */
    private Date gmtModify;

    /**
     * 操作人
     */
    private String modifier;

    /**
     * 审批状态
     */
    private Integer bizStatus;

    /**
     * 商品名称（退款对象为明细时）
     */
    private String goodsName;

    /**
     * 退款金额
     */
    private Double refundMoney;

    /**
     * 备注
     */
    private String remark;
}
