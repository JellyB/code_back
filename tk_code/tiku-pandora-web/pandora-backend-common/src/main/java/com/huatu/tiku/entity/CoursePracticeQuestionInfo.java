package com.huatu.tiku.entity;

import com.huatu.common.bean.BaseEntity;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;

/**
 * Created by lijun on 2019/2/21
 */
@Data
@NoArgsConstructor
@Table(name = "course_practice_question_info")
public class CoursePracticeQuestionInfo extends BaseEntity {

    /**
     * 试题ID
     */
    private Integer questionId;

    /**
     * 百家云 roomID
     */
    private Long roomId;

    /**
     * 开始练习时间
     */
    private Long startPracticeTime;

    /**
     * 设置的练习时长
     */
    private Integer practiceTime;

    /**
     * 统计信息
     */
    private String meta;

    @Builder
    public CoursePracticeQuestionInfo(Integer questionId, Long roomId, Long startPracticeTime, Integer practiceTime, String meta) {
        this.questionId = questionId;
        this.roomId = roomId;
        this.startPracticeTime = startPracticeTime;
        this.practiceTime = practiceTime;
        this.meta = meta;
    }
}
