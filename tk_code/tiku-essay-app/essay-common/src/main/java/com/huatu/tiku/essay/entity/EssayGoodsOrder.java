package com.huatu.tiku.essay.entity;

import com.huatu.tiku.essay.essayEnum.EssayGoodsOrderSourceEnum;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * 批改商品订单表
 * @author zhaoxi
 */
@Entity
@Data
@Builder
@Table(name="v_essay_goods_order")
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@DynamicInsert
public class EssayGoodsOrder extends BaseEntity implements Serializable {


    private int totalMoney;//总金额
    private int realMoney;//实际支付金额
    private int payType;//支付类型(0 支付宝  1微信  2金币   )
    private int userId; //用户id
    private  String comment; //备注信息
    private int incomeType;//   收支类型  0收入  1支出
    private int terminal;//购买平台
    private String orderNumStr;//订单号

    /**
     * 支付时间
     */
    private Date payTime;

    /**
     * 订单来源
     */
    private EssayGoodsOrderSourceEnum source;

    /**
     * 修改人ID
     */
    private Long modifierId;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 课程订单ID
     */
    private Long courseOrderId;

    /**
     * 用户名
     */
    private String name;
}
