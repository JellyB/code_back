package com.huatu.tiku.essay.vo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by x6 on 2017/12/5.
 * 得分点VO
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreVO {

    //得分点
    private String scorePoint;
    //分数
    private double score;
    //序号
    private int sequenceNumber;
    //类型
    private int type;


}
