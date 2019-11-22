package com.huatu.tiku.essay.entity;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Builder
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "v_essay_question_detail")
@EqualsAndHashCode(callSuper = false)
@DynamicUpdate
@DynamicInsert
public class EssayQuestionDetail extends BaseEntity implements Serializable {


    /* 题干 (单题的题目有，套题没有)*/
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
    /* 题型  应用文 议论文  概括题 */
    private int type;
    /*  难度系数 */
    private double difficultGrade;
    /* 是（1）否（0）为真题 */
    private int realQuestion;
    /*  解析作者  */
    private String commentAuthor;

    private String offlineChecker;        //线下审核人


    /* 是否是命题作文 0非命题作文   1命题作文*/
    private int isAssigned;

    /* 批改匹配方式 1关键词匹配   2关键句匹配*/
    private int correctType;
    
    /**
     * 1要点制还是2划档制
     * 对应
     * @ArticleTypeEnum
     */
    private int comprehensiveCorrectType;
}
