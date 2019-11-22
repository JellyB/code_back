package com.huatu.tiku.entity;

import javax.persistence.Table;

import com.huatu.common.bean.BaseEntity;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 随堂练试题统计信息表
 * @author shanjigang
 * @date 2019/3/23 17:48
 */
@Data
@NoArgsConstructor
@Table(name = "courseware_practice_question_info")
public class CoursewarePracticeQuestionInfo extends BaseEntity {
    /**
     * 百家云 roomID
     */
    private Long roomId;

    /**
     * 课件Id coursewareID
     */
    private Long coursewareId;

    /**
     * 统计信息
     */
    private String meta;

    @Builder
    public CoursewarePracticeQuestionInfo( Long roomId, Long coursewareId, String meta) {
        this.roomId = roomId;
        this.coursewareId = coursewareId;
        this.meta = meta;
    }
}
