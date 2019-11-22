package com.huatu.tiku.essay.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * Created by x6 on 2017/12/11.
 * 试题批注 列表页对象
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionAnswerLabelListVO {

    //答题卡id
    private Long answerId;

    //试题id
    private Long questionBaseId;
    private Long questionDetailId;

    //标题
    private String stem;
    // 年份
    private String year;
    // 地区id
    private Long areaId;
    // 地区名称
    private String areaName;
    // 用户id
    private Integer userId;
    // 批改得分
    private Double examScore;
    // 作答时长
    private Integer spendTime;
    // 字数
    private Integer inputWordNum;
    // 批改时间
    private Date correctDate;
    // 状态
    private Integer labelStatus;


    //分差
    private Double subScore;
    /**
     * 分差是否超过满分的10%(1:不超过10%   2:大于10%)
     */
    private Integer subScoreFlag;

    //批注信息列表
    List<LabelSmallVO> labelList;

    //学员作答内容
    private String content;
    //标题
    private String title;
    //题目满分
    private Double score;
    //抄袭度
    private Double copyRatio;
    //最大录入字数
    private Integer maxInputWordNum;
    //最低录入字数
    private Integer minInputWordNum;



    /* 是否是命题作文 0非命题作文   1命题作文*/
    private Integer isAssigned;

    /**
     * 获取下一篇时，
     *    返回对象需要加上totalId
     *    未完成的话返回
     */
    //批注ID
    private Long totalId;

    private Long unfinishedId;



}
