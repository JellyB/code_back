package com.huatu.tiku.essay.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 商品订单
 *
 * @author geek-s
 * @date 2019-07-08
 */
@Data
public class AdminEssayGoodsOrderListDto {

    /**
     * 下单时间-开始
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date gmtCreateBegin;

    /**
     * 下单时间-结束
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date gmtCreateEnd;

    /**
     * 支付时间-开始
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date payTimeBegin;

    /**
     * 支付时间-结束
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date payTimeEnd;

    /**
     * 订单状态
     */
    private Integer bizStatus;

    /**
     * 订单号
     */
    private String orderNumStr;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 用户名
     */
    private String name;
}
