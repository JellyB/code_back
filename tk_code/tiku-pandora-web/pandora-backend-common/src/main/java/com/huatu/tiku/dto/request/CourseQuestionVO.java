package com.huatu.tiku.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhaoxi
 * @Description: 课程关联题目对象
 */
@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
public class CourseQuestionVO {

    //题目id
    private Long questionId;

    //课件id
    private Long courseId;

    //ppt页数
    private int pptIndex;

    //视频时间
    private int position;


}
