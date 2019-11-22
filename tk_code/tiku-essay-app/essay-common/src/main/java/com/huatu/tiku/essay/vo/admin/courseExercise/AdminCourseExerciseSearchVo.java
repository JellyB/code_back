package com.huatu.tiku.essay.vo.admin.courseExercise;

import lombok.*;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/9/3
 * @描述 课后作业搜索vo
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class AdminCourseExerciseSearchVo {

    /* courseWareId 课件ID
     courseType 课程类型 1录入 2直播
     year 年份
     areaId 地区ID
     questionType 试题类型
     searchType 查询条件
     searchContent 查询内容*/
    private Long courseWareId;
    private int courseType;
    private Integer year;
    private Long areaId;
    private Integer questionType;
    private Integer searchType;
    private String searchContent;
    private Integer page = 1;
    private Integer pageSize = 10;
    private Integer type=0;//默认是单题

}
