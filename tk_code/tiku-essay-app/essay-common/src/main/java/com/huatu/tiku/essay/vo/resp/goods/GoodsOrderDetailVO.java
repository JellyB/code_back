package com.huatu.tiku.essay.vo.resp.goods;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author huangqingpeng
 * @title: GoodsOrderDetailVO
 * @description: 用户订单详情内容展示
 * @date 2019-07-0921:40
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class GoodsOrderDetailVO {

    /**
     * 订单详情ID
     */
    private long id;
    /**
     * 订单状态（详情）
     */
    private int bizStatus;
    /**
     * 订单状态名称
     */
    private String bizStatusName;
    /**
     * 订单商品数量
     */
    private int count;
    /**
     * 商品对应批改次数
     */
    private int unit;
    /**
     * 订单详情名称
     */
    private String name;
    /**
     * 过期时间(距离过期时间多长)
     */
    private int expireDate;
    private String expireDateStr;
    /**
     * 过期时长（整体过期时长）
     */
    private int expireTime;
    /**
     * 1表示有时限0表示无期限
     */
    private int expireFlag;
    /**
     * 是否无限次数
     */
    private int isLimitNum;;
    /**
     * 剩余批改次数
     */
    private int num;

    /**
     * 订单编号
     */
    private String orderNumStr;
    /**
     * 付款时间
     */
    private Date payTime;
    /**
     * order.comment
     */
    private String memo;
    /**
     * 订单来源（EssayGoodsOrderSourceEnum）- source
     */
    private String source;

}
