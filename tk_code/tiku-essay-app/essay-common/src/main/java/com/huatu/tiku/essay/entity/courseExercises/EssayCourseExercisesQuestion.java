package com.huatu.tiku.essay.entity.courseExercises;

import com.huatu.tiku.essay.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/8/26
 * @描述 课后作业绑定基础表
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "v_essay_course_exercises_question")
@DynamicUpdate
@DynamicInsert
public class EssayCourseExercisesQuestion extends BaseEntity implements Serializable {

    /**
     * 课件ID
     */
    private Long courseWareId;

    /**
     * 课程类型(直播录播)
     *  1录播 2直播
     */
    private Integer courseType;

    /**
     * 试题id(type 是单题,此字段为试题Id;type 为套题，此字段为套卷Id)
     */
    private Long pQid;

    /**
     * 练习类型(0单题1套题)
     */
    private Integer type;

    /**
     * 排序序号
     */
    private Integer sort;

    /**
     * 智能批改人工批改 (对应correctModeEnum)
     * 1 智能 2 人工 3 智能转人工
     */
    private Integer correctMode;

    /**
     * 批改费用（元/次）
     */
    private Double correctPrice;

    /**
     * 单题时为试题详情id 否则为空
     */
    private Long questionDetailId;
    /**
     * 地区id
     */
    private Long areaId;
    
    /**
     * 地区名称
     */
    private String areaName;


}
