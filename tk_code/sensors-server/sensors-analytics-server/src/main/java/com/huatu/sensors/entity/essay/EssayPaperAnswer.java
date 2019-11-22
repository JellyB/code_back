package com.huatu.sensors.entity.essay;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 试卷答题卡
 */
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "v_essay_paper_answer")
public class EssayPaperAnswer {


    private Long paperBaseId;
    private String name;
    private Integer userId;
    /* 试卷总分 */
    private double score;
    /* 学员所得分数 */
    private double examScore;
    /*  未做答题数量   */
    private Integer unfinishedCount;
    private Integer speed;//平均答题速度
    private Integer lastIndex;//本次答题做到第几题
    /* 答题用时 交卷时更新一次 */
    private Integer spendTime;
    /* 批改时间 */
    private Date correctDate;
    /*  地区ID    */
    private Long areaId;
    /*  地区名称   */
    private String areaName;
    /**
     * 真题 1  模考题 0
     **/
    private Integer type;


    //pdf路径
    private String pdfPath;
    //文件大小
    private String pdfSize;


    //地区排名
    private Integer areaRank;

    //全站排名
    private Integer totalRank;
    //全站总人数
    private Integer totalCount;
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
    private Integer correctMode;
    //人工批改评语
    private String correctRemark;
    //交卷时间
    private Date submitTime;
    //答题卡类型(1普通答题卡 2课后作业)
    private Integer answerCardType;
    
    @Id
    @javax.persistence.GeneratedValue(strategy=javax.persistence.GenerationType.IDENTITY)
	protected Long id;
	protected Integer bizStatus;
	protected Integer status;
	protected String creator;
	protected String modifier;
	protected Date gmtCreate;
	protected Date gmtModify;
}
