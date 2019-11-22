package com.huatu.tiku.course.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by lijun on 2018/8/1
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseBreakpointQuestionDTO {

    /**
     * ID
     */
    private Long id;

    /**
     * 断点名称
     */
    private String pointName;

    /**
     * 播放位置
     */
    private Integer position;

    /**
     * 排列序号
     */
    private Integer sort;

    /**
     * 试题列表
     */
    private List<Long> questionInfoList;

}
