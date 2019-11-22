package com.huatu.tiku.essay.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * 过滤答题卡操作记录
 */
@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name="v_essay_del_answer")
@EqualsAndHashCode(callSuper = false)
@DynamicUpdate
@DynamicInsert
public class EssayDelAnswer extends BaseEntity implements Serializable {


    /**
     * 放弃原因
     */
    private int type;
    /**
     * 答题卡id
     */
    private Long answerId;
}
