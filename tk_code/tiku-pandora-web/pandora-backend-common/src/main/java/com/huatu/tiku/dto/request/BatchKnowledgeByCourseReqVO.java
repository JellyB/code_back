package com.huatu.tiku.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by x6 on 2018/6/22.
 * 通过课程批量查询知识点VO
 * @author zhaoxi
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatchKnowledgeByCourseReqVO {
    /**
     * 课程类型
     */
    private Integer courseType;
    /**
     * 课程id
     */
    private Long courseId;

    /**
     * 知识点id的List
     */
    private List<Long> points;
}
