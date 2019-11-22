package com.huatu.tiku.essay.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 赠送商品详情
 *
 * @author geek-s
 * @date 2019-07-31
 */
@Entity
@Data
@Table(name = "v_essay_reward_record_detail")
@DynamicUpdate
@DynamicInsert
public class EssayRewardRecordDetail extends BaseEntity implements Serializable {

    /**
     * 代报记录ID
     */
    private Long rewardRecordId;

    /**
     * 商品类型
     */
    private Integer type;

    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * 数量
     */
    private Integer count;
}
