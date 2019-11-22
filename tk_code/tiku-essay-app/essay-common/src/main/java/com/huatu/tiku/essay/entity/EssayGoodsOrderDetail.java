package com.huatu.tiku.essay.entity;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by x6 on 2017/11/26.
 * 购买记录详情表\
 */
@Entity
@Data
@Builder
@Table(name = "v_essay_goods_order_detail")
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@DynamicInsert
public class EssayGoodsOrderDetail extends BaseEntity implements Serializable {

    /**
     * 购物记录 id
     **/
    private long recordId;
    /**
     * 商品id
     **/
    private long goodsId;
    /**
     * 商品数量
     **/
    private int count;

    private long userId;    //用户ID
    private int price;//商品价格
    private int unit;//商品对应批改次数
    private int correctMode; //批改商品类型（1智能批改 2人工批改）
    private int goodsType;  //订单类型（GoodsTypeEnum）
    //是否限定次数0不限次数1限定次数
    private int isLimitNum;
    //是否有有效期(0无有效期 1  有有效期)
    private int expireFlag;
    @Temporal(TemporalType.TIMESTAMP)
    private Date expireDate; //有效期时间

    private int expireTime; //有效时长(天为单位)
    private int num;//剩余批改次数


    private Integer specialId;      //特定适用ID（根据goodsType确定是套题ID还是单题ID）

    private String goodsName;   //商品名称

    /**
     * 修改人ID
     */
    private Long modifierId;
}
