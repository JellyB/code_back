package com.huatu.tiku.essay.vo.redis;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by x6 on 2017/12/27.
 * 缓存试卷信息的VO
 *
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EssayQuestionRedisVO {

//    private EssayQuestionBase questionBase;
//    private EssayQuestionDetail questionDetail;

    //试题详情id
    private long baseId;
    //试题详情id
    private long detailId;
    //题序
    private int sort;
    /*  试题所属试卷  */
    private long paperId;
    /*  做题时长  */
    private int limitTime;
    /*  答题要求  文字说明  */
    private String answerRequire;
    /*  输入字数最低限制  */
    private int inputWordNumMin;
    /*  输入字数最多限制  */
    private int inputWordNumMax;
    /* 题型  应用文 议论文  概括题 */
    private int type;
    /*  试题所属年份  */
    private String questionYear;
    /* 试题分数 */
    private double score;
    /**
     * 批改类型
     */
    private int correctType;


}
