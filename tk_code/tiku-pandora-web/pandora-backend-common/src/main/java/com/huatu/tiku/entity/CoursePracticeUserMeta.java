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
@Table(name = "course_practice_user_meta")
public class CoursePracticeUserMeta extends BaseEntity {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 积分
     */
    private Integer integral;

    /**
     * 答题卡ID
     */
    private Long answerCardId;

    /**
     * 直播房间ID
     */
    private Long roomId;

    /**
     * 课程ID
     */
    private Long courseId;

    @Builder
    public CoursePracticeUserMeta(Long userId, String userName, Integer integral, Long answerCardId, Long roomId, Long courseId) {
        this.userId = userId;
        this.userName = userName;
        this.integral = integral;
        this.answerCardId = answerCardId;
        this.roomId = roomId;
        this.courseId = courseId;
    }
}
