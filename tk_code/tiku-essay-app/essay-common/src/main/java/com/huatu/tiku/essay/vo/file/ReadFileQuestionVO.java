package com.huatu.tiku.essay.vo.file;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by x6 on 2018/3/22.
 * 读文件获取的question对象
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReadFileQuestionVO {

    //选项信息
    private List<String> options;


//    private Integer answerCount;

    //题干信息
    private String content;

    //正确答案
    private String correctChoice;

    //正确率
    private String correctRatio;

//    private long createdTime;

    //难度级别
    private double difficulty;


    //易错答案
    private String mostWrongAnswerChoice;

    //知识点
    private String point;

    //解析
    private String solution;
   //来源
    private String source;
//    private List tags;
//    private long totalCount;
//    private long type;
//    private long wrongCount;





}
