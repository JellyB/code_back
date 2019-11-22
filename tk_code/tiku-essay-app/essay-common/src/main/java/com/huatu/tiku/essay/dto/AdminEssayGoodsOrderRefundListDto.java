package com.huatu.tiku.essay.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 商品订单退款
 *
 * @author geek-s
 * @date 2019-07-08
 */
@Data
public class AdminEssayGoodsOrderRefundListDto {

    /**
     * 申请时间-开始
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date gmtCreateBegin;

    /**
     * 申请时间-结束
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date gmtCreateEnd;

    /**
     * 订单号
     */
    private String orderNumStr;

    /**
     * 退款状态
     */
    private Integer bizStatus;
}
