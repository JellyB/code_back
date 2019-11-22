package com.huatu.tiku.essay.entity;

import com.huatu.tiku.essay.essayEnum.EssayCorrectGoodsSaleTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 批改商品表
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "v_essay_correct_goods")
@EqualsAndHashCode(callSuper = false)
@DynamicUpdate
@DynamicInsert
public class EssayCorrectGoods extends BaseEntity implements Serializable {

    private String name; //批改商品名称
    private int type; //批改类型（0单题批改 1多题批改 2议论文（7.0将议论文单独提出来了））
    private int inventory;//（库存）可售数量
    private int salesNum;  //已售数量
    private int price;//价格
    private int num;//商品对应批改次数
    private int activityPrice; //活动价格

    private int correctMode; //批改商品类型（1智能批改 2人工批改）
    private Integer isLimitNum;
    //是否有有效期(0无有效期 1  有有效期)
    private int expireFlag;
    private int expireDate; //有效期时间（天）
    private int sort;// 排序标识（标准答案 套题批改，议论文批改的顺序）

    /**
     * 销售类型
     */
    private EssayCorrectGoodsSaleTypeEnum saleType;
}
