package com.huatu.tiku.essay.vo.admin;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.huatu.tiku.essay.entity.EssayStandardAnswer;
import com.huatu.tiku.essay.vo.resp.EssayStandardAnswerVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
* Created by huangqp on 2017\12\13 0013.
*/
@Builder
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminQuestionVO {
    /* 题干 */
    private String stem;
    /*  答题要求  文字说明  */
    private String answerRequire;
    /*  输入字数最低限制  */
    private int inputWordNumMin;
    /*  输入字数最多限制  */
    private int inputWordNumMax;
    /* 参考答案 */
    private String answerComment;

    /*  标题  */
    private String topic;
    /* 子标题 */
    private  String subTopic;


    /* 称呼 */
    private String callName;
    /* 落款日期 */
    private String inscribedDate;
    /* 落款名称 */
    private String inscribedName;
    /* 落款名称 */
    private List<String> inscribedNameList;
    //答案列表（多個答案）
    private List<EssayStandardAnswerVO> answerList;


    /* 答题范围 */
    private String answerRange;
    /* 答题细节（要求） */
    private String answerDetails;
    /* 答题任务 */
    private String answerTask;


    /* 阅卷规则描述 */
    private String correctRule;
    /* 权威点评 */
    private String authorityReviews;
    /* 审题要点  试题分析 */
    private String analyzeQuestion;
    /* 解题思路 */
    private String answerThink;
    /* 赋分说明 */
    private String bestowPointExplain;
    /* 试题分数 */
    private double score;
    /* 题型名称 （形式为：上级名称/下级名称）*/
    private String questionTypeName;
    private List<Long> questionType;
    /* 题型id  1概括2综合分析3对策4应用文5议论文 */
    private int type;
    /*  难度系数 */
    private double difficultGrade;
    /* 是（1）否（0）为真题 */
    private int realQuestion;
    /*  解析作者  */
    private String commentAuthor;
    //材料序号
    private int sort;
    private long paperId;
    private long areaId;
    private String areaName;
    private String questionYear;
    private String questionDate;
    private long questionBaseId;
    private long questionDetailId;
    private int status ;
    private int bizStatus ;

    private String paperName;
    /*资料集合*/
    private List<Integer> ziliaos;

    //是否是缺失题（0  不缺失   1 缺失）
    private int isLack;

    /* 是否是命题作文 0非命题作文   1命题作文*/
    private int isAssigned;
    /* 批改匹配方式 1关键词匹配   2关键句匹配*/
    private int correctType;
    /**
     * 视频ID
     */
    private Integer videoId;
    /**
     * 视频播放地址
     */
    private String videoUrl;
    
    /**
     * 要点制还是划档值
     * @ArticleTypeEnum
     */
    private int comprehensiveCorrectType;

    public AdminQuestionVO(){
        this.setStem("");
        this.setAnswerRequire("");
        this.setAnswerComment("");
        this.setCorrectRule("");
        this.setAuthorityReviews("");
        this.setAnswerTask("");
        this.setAnswerDetails("");
        this.setAnswerRange("");
        this.setAnalyzeQuestion("");
        this.setAnswerThink("");
        this.setBestowPointExplain("");
        this.setCommentAuthor("");
    }
}
