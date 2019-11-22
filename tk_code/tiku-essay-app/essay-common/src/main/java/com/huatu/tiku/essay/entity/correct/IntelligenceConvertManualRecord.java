package com.huatu.tiku.essay.entity.correct;

import com.huatu.tiku.essay.entity.BaseEntity;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/8/8
 * @描述
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "v_essay_intelligence_convert_manual_record")
@EqualsAndHashCode(callSuper = false)
@DynamicInsert(true)//动态插入
@DynamicUpdate(true)//动态更新
@Builder
public class IntelligenceConvertManualRecord extends BaseEntity {

    //答题卡ID
    private long intelligenceAnswerId;

    private Integer answerType;
    /**
     * 新订单的ID
     */
    private long orderId;

    private long manualAnswerId;

}
