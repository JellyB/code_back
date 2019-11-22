package com.huatu.tiku.essay.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by x6 on 2017/11/24.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EssayPaperVO {

    //id
    private Long paperId;
    private Integer limitTime;
    private Double score;
    //名称
    private String paperName;

    //智能批改次数
    private int correctNum;
    //用户最近一次智能批改答题的情况，0空白1未交卷2交卷未批改
    private int recentStatus;
    //答题卡id
    private Long answerCardId;
    //操作类型(0 保存  1交卷)
    private Integer saveType;
    /*  未作答题数量   */
    private Integer unfinishedCount;
    /*  本次答题做到第几题  */
    private Integer lastIndex;
    // 答题用时
    private Integer spendTime;

    //总题目数
    private Integer totalCount;
    /**
     * 全站智能批改次数
     */
    private Integer correctSum;


    //是否上线
    private Boolean isOnline;

    //是否可点击
    private Boolean isAvailable;
    /**
     * 是否有存在视频解析
     */
    private Boolean videoAnalyzeFlag;

    /**
     * 视频解析id
     */
    private int courseId;
    /**
     * 解析课简介
     */
    private String courseInfo;
    /**
     * 地区id
     */
    private long areaId;
    
    /**
     * 人工批改次数
     */
    private Integer manualNum;
    /**
     * 全站人工批改总次数
     */
    private Integer manualSum;
    
    /**
     * 最近一次人工批改的答题卡状态
     */
    private Integer manualRecentStatus;
    
    /**
     * 最近修改的答题卡状态
     */
    private Integer lastType;
    
    /**
     * 批改类型
     */
    private Integer correctMode;
    
    /**
     * 另外一种答题卡id
     */
    private Long otherAnswerCardId;
    
    /**
     * 真题卷还是模考卷1真题卷0模考卷
     */
    private Integer type;

    private long startTime;

    private long endTime;
}
