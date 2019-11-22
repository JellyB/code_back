package com.huatu.tiku.essay.entity.vo.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by linkang on 17-7-13.
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class MatchCardUserMeta implements Serializable {
    private static final long serialVersionUID = 1L;

    private int positionId; //职位id
    private String positionName; //职位名称
    private Line scoreLine; //模考大赛分数
    private int positionRank; //职位排名
    private int positionCount; //职位报名人数
    private double positionAverage; //职位平均分
    private double positionMax; //职位最高分
    private int positionBeatRate; //职位击败百分比
}
