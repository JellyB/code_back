package com.huatu.tiku.dto;

import com.huatu.tiku.entity.advice.QuestionAdvice;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author zhengyi
 * @date 2018/10/11 4:07 PM
 **/
@Data
@NoArgsConstructor
public class QuestionAdviceVo {
    private Long id;
    private int questionType;
    private String questionTypeName;
    private String pointsName;
    private String from;
    private String stem;
    private String analysis;
    private String extend;
    private List<String> choices;
    private int availFlag = 1;
    private int status;
    /**
     * 材料
     */
    private List<String> materialContent;
    private String answer;
    private List<QuestionAdviceVo> children;
    private List<QuestionAdvice> userErrorDescriptions;
}