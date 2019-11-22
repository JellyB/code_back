package com.huatu.tiku.entity;

import com.huatu.common.bean.BaseEntity;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

/**
 * 课程-断点信息表
 * Created by lijun on 2018/6/10
 */
@Data
@NoArgsConstructor
@Table(name = "course_breakpoint")
public class CourseBreakpoint extends BaseEntity {

    /**
     * 断点名称
     */
    @NotBlank(message = "插入知识点名称不能为空")
    private String pointName;

    /**
     * 播放位置
     */
    @NotNull(message = "插入知识点位置不能为空")
    private Integer position;

    /**
     * 课程ID
     */
    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    /**
     * 课程类型
     */
    @NotNull(message = "课程类型不能为空")
    private Integer courseType;

    /**
     * 排列序号
     */
    private Integer sort;

    /**
     * 试题数量
     */
    @Transient
    private Long questionCount;

    @Builder
    public CourseBreakpoint(Long id, Integer bizStatus, Integer status, Long creatorId, Timestamp gmtCreate, Long modifierId, Timestamp gmtModify, String pointName, Integer position, Long courseId, Integer courseType, Integer sort) {
        super(id, bizStatus, status, creatorId, gmtCreate, modifierId, gmtModify);
        this.pointName = pointName;
        this.position = position;
        this.courseId = courseId;
        this.courseType = courseType;
        this.sort = sort;
    }


}
