package com.huatu.tiku.course.bean.vo;

import com.huatu.tiku.course.bean.practice.QuestionMetaBo;
import lombok.*;

import java.util.List;

/**
 * @author shanjigang
 * @date 2019/3/23 22:22
 */
@NoArgsConstructor
@Setter
@Getter
public class CoursewarePracticeQuestionVo {
    private Long roomId;
    private Long coursewareId;
    private List<QuestionMetaBo> meta;
}
