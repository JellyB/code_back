package com.huatu.tiku.essay.vo.resp;

import com.huatu.tiku.essay.essayEnum.CorrectModeEnum;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by x6 on 2017/11/28.
 * 创建答题卡对象
 */
@Builder
@NoArgsConstructor
@Data
public class EssayCourseWorkAnswerCardVO {


    private Long paperBaseId;
    private Long questionBaseId;
    private Integer userId;

    private Integer type;
    /**
     * 批改类型
     * @see CorrectModeEnum
     */
    private Integer correctMode;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 大纲ID
     */
    private Long syllabusId;

    /**
     * 课件ID
     */
    private Long courseWareId;

    private int correctNum;

    public EssayCourseWorkAnswerCardVO(Long paperBaseId, Long questionBaseId, Integer userId, Integer type, Integer correctMode, Long courseId, Long syllabusId, Long courseWareId, int correctNum) {
        this.paperBaseId = paperBaseId;
        this.questionBaseId = questionBaseId;
        this.userId = userId;
        this.type = type;
        this.correctMode = correctMode;
        this.courseId = courseId;
        this.syllabusId = syllabusId;
        this.courseWareId = courseWareId;
        this.correctNum = correctNum;
    }
}
