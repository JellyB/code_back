package com.huatu.tiku.essay.entity;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * 答题卡表
 */
@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "v_essay_question_answer")
@EqualsAndHashCode(callSuper = false)
@DynamicUpdate
@DynamicInsert
public class EssayQuestionAnswer extends BaseEntity implements Serializable {

    //Bizstatus 0 空白  1未完成  2 已交卷  3已批改

    //学员答题内容
    private String content;
    //批改后的内容
    private String correctedContent;
    /* 学员所得分数 */
    private double examScore;
    /*  试题总分 */
    private double score;
    /*  用户id    */
    private int userId;
    /*  地区ID    */
    private long areaId;
    /*  地区名称   */
    private String areaName;
    /*  试题ID    */
    private long questionBaseId;
    /*  试题Detail表  ID  冗余 方便统计    */
    private long questionDetailId;
    /*  试卷答题卡id    */
    private long paperAnswerId;
    /*  试题所属年份  */
    private String questionYear;
    /*  试题所属试卷  */
    private long paperId;
    /*  答题终端: pc,移动  */
    private int terminal;
    /* 答题用时 交卷时更新一次  */
    private int spendTime;
    /* 批改时间 */
    private Date correctDate;
    /* 学员答案字数 */
    private int inputWordNum;
    /* 学员答题速度 */
    private int speed;

    //试题类型
    private int questionType;


    //pdf路径
    private String pdfPath;
    //文件大小
    private String pdfSize;
    //文件名字（拍照答题图片名称）
    private String fileName;


    /**
     * 批注次数(默认0次，批注一次，两次，终审分别对应 1,2,3)
     */
    private int labelStatus;
    /**
     * 分差
     */
    private Double subScore = -1D;

    /**
     * 分差百分比
     */
    private Double subScoreRatio;
    /**
     * 分差是否超过满分的10%(1:不超过10%   2:大于10%)
     */
    private Integer subScoreFlag;
    /**
     * 抄袭度（查询批注列表的时候，算一次然后存进来，之后不再计算）
     */
    private double copyRatio;

    /**
     * 批改类型
     */
    private int correctType;

    //1智能批改2人工批改3智能转人工批改
    private Integer correctMode;
    //评语信息
    private String correctRemark;
    //单题题目名称
    private String name;
    //交卷时间
    private Date submitTime;
    //答题卡类型(1普通答题卡 2课后作业)
    private Integer answerCardType;
}
