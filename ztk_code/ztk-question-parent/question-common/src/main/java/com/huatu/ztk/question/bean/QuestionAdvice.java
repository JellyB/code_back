package com.huatu.ztk.question.bean;

import lombok.*;

import java.io.Serializable;

/**
 * 用户试题纠错bean
 * Created by shaojieyue
 * Created time 2016-07-22 09:34
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
@ToString
public class QuestionAdvice  implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;//id
    private int qid;//要纠错的试题id
    private long uid;//纠错人
    private int qtype;//试题类型
    private int moduleId;//试题模块id
    private int questionArea;//试题区域
    private int catgory;//所属科目
    private int subject;//所属类目
    private int errorType;//错误类型
    private String contacts;//联系方式
    private String content;//纠错内容
    private int userArea;
}
