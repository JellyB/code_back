package com.huatu.tiku.essay.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.huatu.tiku.essay.essayEnum.CorrectModeEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by x6 on 2017/11/28.
 * 创建答题卡对象
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateAnswerCardVO {


    private Long paperBaseId;
    private Long questionBaseId;
    private Integer userId;
    private int terminal;

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
     * 课件类型
     */
    private Integer courseType;

    /**
     * 课件ID
     */
    private Long courseWareId;
}
