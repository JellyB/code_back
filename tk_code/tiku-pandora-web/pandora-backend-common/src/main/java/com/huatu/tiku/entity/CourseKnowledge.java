package com.huatu.tiku.entity;

import com.huatu.common.bean.BaseEntity;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * 课程 -知识点 关联表
 * Created by lijun on 2018/6/10
 */
@Data
@NoArgsConstructor
@Table(name = "course_knowledge")
public class CourseKnowledge extends BaseEntity {
    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 课件类型
     */
    private Integer courseType;

    /**
     * 知识点ID
     */
    private Long knowledgeId;

    @Builder
    public CourseKnowledge(Long id, Integer bizStatus, Integer status, Long creatorId, Timestamp gmtCreate, Long modifierId, Timestamp gmtModify, Long courseId, Integer courseType, Long knowledgeId) {
        super(id, bizStatus, status, creatorId, gmtCreate, modifierId, gmtModify);
        this.courseId = courseId;
        this.courseType = courseType;
        this.knowledgeId = knowledgeId;
    }
}
