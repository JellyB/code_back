package com.huatu.tiku.essay.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/*
 *
 * 单题所属不同试卷不同地区不同年份VO
 * */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EssayQuestionAreaVO {
    //单题 id
    private Long id;
    /*  所属地区试卷名称  例： 安徽A卷   浙江甲卷   北京卷  */
    private String name;

    /*  试题所属年份  */
    private String questionYear;

    /*  试题所属日期  */
    private String questionDate;

    /* 地区id */
    private Long areaId;
    /* 地区名称 */
    private String areaName;

    /* 答题状态 */
    private Integer bizStatus;
    /*  用户智能批改次数 */
    private Integer correctTimes;

    /*  答题时限 */
    private Integer limitTime;

    private List<EssayQuestionAreaVO> essayQuestionBelongPaperVOList;
    /* base试题id */
    private Long questionBaseId;
    /* detail试题id */
    private Long questionDetailId;
    /* base试卷id */
    private Long paperId;
    /**
     * 批改总次数
     */
    private Integer correctSum;
    //视频id
    private Integer videoId;

    /* 我的用户智能批改次数 */
    private Integer correctNum;

    /* 我的人工批改次数 */
    private Integer manualNum;

    /**
     * 最近一次人工批改的答题卡状态
     */
    private Integer manualRecentStatus;

    /**
     * 最近一次智能批改状态
     */
    private Integer recentStatus;

    /**
     * 最近修改的答题卡状态
     */
    private Integer lastType;

    /**
     * 试题类型
     */
    private Integer questionType;

    /**
     * 批改方式
     */
    private Integer correctMode;
}
