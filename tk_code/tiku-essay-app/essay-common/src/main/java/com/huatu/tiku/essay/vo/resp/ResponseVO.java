package com.huatu.tiku.essay.vo.resp;

import java.util.List;

/**
 * Created by x6 on 2017/12/8.
 */

import com.fasterxml.jackson.annotation.JsonInclude;
import com.huatu.tiku.essay.entity.EssayQuestionAnswer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by x6 on 2017/12/1.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public  class ResponseVO {
    /* 校验用户批改次数 */
    //是否存在剩余次数(0 存在 1不存在)
    private Integer exist;

    /* 创建答题卡 */
    //答题卡id
    private Long answerCardId;




    /* 查询用户剩余批改次数 */
    /**
     * 总剩余批改次数
     */
    private Integer correctSum;
    /**
     * 单题剩余批改次数
     */
    private Integer singleNum;
    /**
     * 套题剩余批改次数
     */
    private Integer multiNum;
    /**
     * 议论文剩余批改次数(v7.0)
     */
    private Integer argumentationNum;
    /**
     *  无限次批改截止日期
     */
    private String endDate;

    /* 校验用户某道单题、试卷批改次数是否可以再批改(0 可以 1不可以) */
    private Integer canCorrect;
    /* 最大批改次数 */
    private Integer maxCorrectTimes;

    //批改是否免费
    private String essayGoodsFree;


    //模考剩余作答时间
    private Integer leftTime;
    //反馈成功标志
    private boolean flag;
    
    /**
     * 试题答题卡
     */
    private List<EssayQuestionAnswerSimpleVO> questionAnswerCardList;



}
