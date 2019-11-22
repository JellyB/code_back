package com.huatu.splider.task;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author hanchao
 * @date 2018/2/23 13:25
 */
@Data
public class Question {

    private int id;
    private String content;
    private Object material;
    private int type;
    private int difficulty;
    private long createdTime;
    private Object shortSource;
    private Map correctAnswer;
    private List<Map> accessories;
    //solutions
    private Object flags;
    private Object tags;
    private Object solutionAccessories;
    private String solution;
    private String source;
    //
    private Object point;
    //
    private int answerCount;
    private int wrongCount;
    private int totalCount;
    private Object mostWrongAnswer;
    private String correctRatio;

}
