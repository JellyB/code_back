package com.huatu.tiku.interview.entity.vo.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by x6 on 2018/4/11.
 * 交卷VO（课堂互动，全真模考）
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaperCommitVO {

    private long id;
    //---------答题日期---------
    private String answerDate;
    // -----姓名---------
    private String name;
    //---------openId---------
    private String openId;

    //推送id
    private long pushId;
    //试卷id
    private long paperId;
    //语音语调
    private String pronunciation;
    //流畅程度
    private String fluencyDegree;
    //仪态动作
    private String deportment;
    //试题列表
    private List<QuestionCommitVO> questionCommitVOList;

    //其他评价
    private String elseRemark;
    //综合评分
    private Double overAllScore;
}
