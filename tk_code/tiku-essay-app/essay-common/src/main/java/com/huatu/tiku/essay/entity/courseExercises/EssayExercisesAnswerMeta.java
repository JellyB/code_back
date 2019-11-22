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
import java.util.Date;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/8/26
 * @描述
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "v_essay_exercises_answer_meta")
@DynamicUpdate
@DynamicInsert
public class EssayExercisesAnswerMeta extends BaseEntity implements Serializable {

    /**
     * 答题卡ID
     */
    private Long answerId;

    /**
     * 答题卡类型(0 单题 1 套题)
     */
    private Integer answerType;

    /**
     * 试卷试题ID
     */
    private Long pQid;

    /**
     * 批改次数(第一次批改 还是第二次批改)
     */
    private Integer correctNum;
    /**
     * 学员成绩
     */
    private Double examScore;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 大纲ID
     */
    private Long syllabusId;

    /**
     * 课件ID
     */
    private Long courseWareId;

    /**
     * 用户ID(备注：userName和用户头像批量查询)
     */
    private Integer userId;

    /**
     * 用时
     */
    private Long spendTime;

    /**
     * 交卷时间
     */
    private Date submitTime;

    /**
     * 课程类型(直播录播)
     * 1 录播;2 直播
     */
    private Integer courseType;


}
