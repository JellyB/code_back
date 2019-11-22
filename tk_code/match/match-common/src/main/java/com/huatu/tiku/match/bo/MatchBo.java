package com.huatu.tiku.match.bo;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 描述：模考大赛获取信息封装
 *
 * @author biguodong
 * Create time 2018-10-18 下午3:28
 **/

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.ALWAYS)
public class MatchBo implements Serializable {

    /**
     * 试卷id
     */
    private int matchId;

    /**
     * 大赛名称
     */
    private String name;

    /**
     * 时间信息
     */
    private String timeInfo;

    /**
     * 考试说明
     */
    private String instruction;

    /**
     * 考试说明
     */
    private String instructionPC;

    /**
     * 标签  用来区别2018国考/省考
     */
    private int tag;

    /**
     * 考试科目
     */
    private int subject;

    /**
     * 考试开始时间
     */
    private long startTime;

    /**
     * 结束时间
     */
    private long endTime;

    /**
     * 数据状态 - MatchStatusEnum
     */
    private int status;

    /**
     * 申论模考大赛试卷id
     */
    private long essayPaperId;

    /**
     * 申论模考大赛开始时间
     */
    private long essayStartTime;

    /**
     * 申论模考大赛结束时间
     */
    private long essayEndTime;

    /**
     * 1只有行测报告 2只有申论报告 3行测申论报告都有 - MatchInfoEnum.FlagEnum
     */
    private int flag;

    /**
     * 1表示行测模考大赛阶段 2表示申论模考大赛阶段 - MatchInfoEnum.StageEnum
     */
    private int stage;

    /**
     * 总报名人数
     */
    private int enrollCount;

    /**
     * 报名方式 0选择地区报名 1无地区报名 - MatchInfoEnum.EnrollFlagEnum
     */
    private int enrollFlag;

    /**
     * 用户信息
     */
    private MatchUserMetaBo userMeta;

    /**
     * 模考大赛解析课信息
     */
    private CourseInfoBo courseInfo;

    /**
     * 大礼包信息
     */
    private String iconUrl;
}
