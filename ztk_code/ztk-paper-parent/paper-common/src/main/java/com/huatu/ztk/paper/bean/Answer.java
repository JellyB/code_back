package com.huatu.ztk.paper.bean;


import lombok.Data;

import java.io.Serializable;

/**
 * 试题答案bean
 * Created by shaojieyue
 * Created time 2016-05-03 14:45
 */
@Data
public class Answer implements Serializable {
    private static final long serialVersionUID = 1L;
    /*试题ID*/
    private int questionId;
    private String answer;
    /*是否正确*/
    private int correct;
    /*做题时长*/
    private int time;
    /*用户是否有疑问*/
    private int doubt;
}
