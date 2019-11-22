package com.huatu.tiku.essay.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 商品订单详情
 *
 * @author geek-s
 * @date 2019-07-08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminEssayGoodsOrderDetailWrapperVO {

    /**
     * ID
     */
    private Long id;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 订单号
     */
    private String orderNumStr;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 下单时间
     */
    private Date gmtCreate;

    /**
     * 支付时间
     */
    private Date payTime;

    /**
     * 总金额
     */
    private Double totalMoney;

    /**
     * 实际支付金额
     */
    private Double realMoney;

    /**
     * 支付类型(0 支付宝 1 微信 2 金币)
     */
    private Integer payType;

    /**
     * 订单来源
     */
    private Integer source;

    /**
     * 订单状态
     */
    private Integer bizStatus;

    /**
     * 购买次数（总次数）
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
     * 可使用次数（有效期内）
     */
    private Integer usableAmount;

    /**
     * 过期时间（显示最长）
     */
    private Date expireDate;

    /**
     * 订单明细
     */
    private List<AdminEssayGoodsOrderDetailVO> orderDetails;

    /**
     * 用户名
     */
    private String name;
}
