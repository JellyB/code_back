package com.huatu.tiku.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhaoxi
 * @Description: 课程对象
 */
@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
public class CourseCheckVO {

    //课件id
    private Long id;

    //课件时长（单位：分钟）
    private int timeLength;

    //视频类型
    private int type;

    //所属科目
    private long subject;



}
