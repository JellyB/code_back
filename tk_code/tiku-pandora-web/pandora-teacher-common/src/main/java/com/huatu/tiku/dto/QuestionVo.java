package com.huatu.tiku.dto;

import lombok.Data;

import java.util.List;

/**
 * @author zhengyi
 * @date 2018/11/6 6:11 PM
 **/
@Data
public class QuestionVo {
    private int id;
    private int questionType;
    private String questionTypeName;
    private String from;
    private String stem;
    private String analysis;
    private String extend;
    private List<String> choices;
    private List<String> pointsName;
    private int availFlag = 1;
    private String answer;
}