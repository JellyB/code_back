package com.huatu.ztk.paper.bean;

import lombok.*;

import java.io.Serializable;

/**
 * Created by shaojieyue
 * Created time 2016-07-04 12:16
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class CardUserMeta implements Serializable {
    private static final long serialVersionUID = 1L;

    private int rank;//排名
    private int total;//该试卷总的答题卡数量
    private double average;//平均分
    private int beatRate;//击败的比率
    private double max; //最高分

    private int submitRank; //交卷次序（第多少位完成考试）
    private int submitCount; //交卷人数
    private int rNumMax;        //最高答对题数
    private int rNumAverage;    //平均答对题数
    private long reportTime;   //统计更新时间

    //新版本分数添加返回字符串
    private String averageStr;
    private String maxStr;


}
