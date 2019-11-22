package com.huatu.tiku.essay.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * Created by x6 on 2018/6/5.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EssayOrderVO {

    /**
     * 订单id
     */
    private Long id;
    /**
     * 订单支付状态
     *          PAYED(1, "支付成功"), CANCEL(2, "取消支付"),PAYEXCEPTION(3, "支付异常"),INIT(0, "初始状态（未支付）");
     */
    private Integer bizStatus;
    /**
     * EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.getName(detail.getBizStatus())
     */
    private String bizStatusName;
    /**
     * 总金额
     */
    private Integer totalMoney;

    /**
     * 实际支付金额
     */
    private Integer realMoney;
    /**
     * 支付类型(0 支付宝  1微信  2金币   )
     */
    private Integer payType;
    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 订单编号
     */
    private String orderNumStr;


    /**
     * 下单时间
     */
    private Date createTime;
    /**
     * 支付时间
     */
    private Date payTime;
    /**
     * 订单取消时间
     */
    private Date cancelTime;
    /**
     * 订单如果不支付，被关闭时间
     */
    private Date closeTime;
    /**
     * 剩余时间（单位：秒）
     */
    private long leftTime;





    /**
     * 商品列表
     */
    private List<OrderGoodsVO> goodsList;

}
