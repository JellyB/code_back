package com.huatu.tiku.essay.vo.admin;


import com.huatu.tiku.essay.constant.status.EssayCorrectGoodsConstant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

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
public class AdminEssayGoodsOrderDetailVO {

    /**
     * 订单明细ID
     */
    private Long id;

    /**
     * 商品ID
     */
    Long goodsId;

    /**
     * 商品名称
     */
    String goodsName;

    /**
     * 批改功能类型（1智能批改 2人工批改）
     */
    private Integer correctMode;

    /**
     * 批改功能类型
     */
    private String correctModeName;

    public void setCorrectMode(Integer correctMode) {
        this.correctMode = correctMode;
        this.correctModeName = EssayCorrectGoodsConstant.CorrectTypeEnum.of(correctMode).getName();
    }

    /**
     * 批改商品类型
     */
    private Integer goodsType;

    /**
     * 批改商品类型
     */
    private String goodsTypeName;

    public void setGoodsType(Integer goodsType) {
        this.goodsType = goodsType;
        this.goodsTypeName = EssayCorrectGoodsConstant.GoodsTypeEnum.create(goodsType).getName();
    }

    /**
     * 总次数
     */
    private Integer totalAmount;

    /**
     * 已使用次数
     */
    private Integer usedAmount;

    /**
     * 剩余次数
     */
    private Integer restAmount;

    /**
     * 有效期时间
     */
    private Date expireDate;

    /**
     * 商品价格
     */
    private Double price;

    /**
     * 订单状态
     */
    private Integer bizStatus;

    /**
     * 总金额
     */
    private Double totalMoney;
}