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
 * 试卷答题卡
 */
@Builder
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate(true)
@Table(name = "v_essay_paper_answer")
@EqualsAndHashCode(callSuper = false)
public class EssayPaperAnswer extends BaseEntity implements Serializable {


    //    private  long paperDetailId;
    private long paperBaseId;
    private String name;
    private int userId;
    /* 试卷总分 */
    private double score;
    /* 学员所得分数 */
    private double examScore;
    /*  未做答题数量   */
    private int unfinishedCount;
    private int speed;//平均答题速度
    private int lastIndex;//本次答题做到第几题
    /* 答题用时 交卷时更新一次 */
    private int spendTime;
    /* 批改时间 */
    private Date correctDate;
    /*  地区ID    */
    private long areaId;
    /*  地区名称   */
    private String areaName;
    /**
     * 真题 1  模考题 0
     **/
    private int type;


    //pdf路径
    private String pdfPath;
    //文件大小
    private String pdfSize;


    //地区排名
    private int areaRank;

    //全站排名
    private int totalRank;
    //全站总人数
    private int totalCount;
    //全站排名升降值
    private Integer totalRankChange;


    //对应最高分
    private double maxScore;
    //对应最高分
    private double avgScore;
    //考试成绩升降值
    private Double examScoreChange;
    //对应最高分升降值
    private Double maxScoreChange;
    //1智能批改2人工批改3智能转人工批改
    @Column(columnDefinition = "smallint default 1")
    private Integer correctMode;
    //人工批改评语
    private String correctRemark;
    //交卷时间
    private Date submitTime;
    //答题卡类型(1普通答题卡 2课后作业)
    private Integer answerCardType;
}
