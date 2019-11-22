package com.huatu.tiku.entity;

import com.huatu.common.bean.BaseEntity;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

/**
 * 课程-断点 -试题关联表
 * Created by lijun on 2018/6/10
 */
@Data
@NoArgsConstructor
@Table(name = "course_breakpoint_question")
public class CourseBreakpointQuestion extends BaseEntity {

    /**
     * 断点ID
     */
    @NotNull(message = "插入的节点ID不能为空")
    private Long breakpointId;

    /**
     * 试题ID
     */
    @NotNull(message = "试题ID不能为空")
    private Long questionId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 排列序号
     */
    @NotNull(message = "排列序号不能为空")
    private Integer sort;

    /**
     * 是否显示题干 - 默认显示 1 - 不显示 - 0
     */
    private Integer displayStem;

    /**
     * PPT 下标位置
     */
    private Integer pptIndex;

    @Builder
    public CourseBreakpointQuestion(Long id, Integer bizStatus, Integer status, Long creatorId, Timestamp gmtCreate, Long modifierId, Timestamp gmtModify, Long breakpointId, Long questionId, Long userId, Integer sort, Integer displayStem, Integer pptIndex) {
        super(id, bizStatus, status, creatorId, gmtCreate, modifierId, gmtModify);
        this.breakpointId = breakpointId;
        this.questionId = questionId;
        this.userId = userId;
        this.sort = sort;
        this.displayStem = displayStem;
        this.pptIndex = pptIndex;
    }
}
