package com.huatu.tiku.essay.entity.correct;

import com.huatu.tiku.essay.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author huangqingpeng
 * @title: CorrectOrderSnapshot
 * @description: 老师批改订单行为日志
 * @date 2019-07-1714:47
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "v_essay_correct_order_snapshot")
@DynamicUpdate
@DynamicInsert
public class CorrectOrderSnapshot extends BaseEntity {

    /**
     * 订单ID
     */
    private Long orderId;
    /**
     * 订单操作ID
     */
    private Integer operate;
    /**
     * 订单操作名称
     */
    private String operateName;
    /**
     * 操作描述（根据不同的操作行为存储不同的JSON数据，解析方式不同）
     */
    private String description;
    /**
     * 操作渠道（0后台1老师2学员）
     */
    private Integer channel;

    /**
     * 批改老师ID(每条记录都保存下批改老师ID,查询必须)
     */
    private Long correctTeacherId;

}
