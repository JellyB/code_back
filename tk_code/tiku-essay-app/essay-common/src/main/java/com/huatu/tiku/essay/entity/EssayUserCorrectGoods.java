package com.huatu.tiku.essay.entity;

import lombok.*;

import javax.persistence.*;

import com.huatu.tiku.essay.constant.status.EssayCorrectGoodsConstant.CorrectGoodsBizStatusEnum;
import com.huatu.tiku.essay.constant.status.EssayCorrectGoodsConstant.GoodsTypeEnum;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户和批改商品关系表
 */

@Builder
@Entity
@Table(name="v_essay_user_correct_goods")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@DynamicInsert
public class EssayUserCorrectGoods extends BaseEntity implements Serializable {
	/**
	 * @see GoodsTypeEnum
	 */
    private  int type;//类型(0普通单题，1套题，2议论文)
    private  int usefulNum;//可用数量
    private  int totalNum;//总数量
    private  int userId; //用户id

    private int specialNum; //专用数量（只针对特定的试卷或试题可用）
    private int isLimitNum; //是否限定次数
    /**
     * 过期时间
     * 取最近将过期的批改次数过期时间，如果当前时间大于改时间，则重新统计批改次数记录
     * 字段为空时，表示无过期数据，不做上述判断
     */
    @Temporal(TemporalType.TIMESTAMP)
    @org.hibernate.annotations.CreationTimestamp
    protected Date expireTime;

}
