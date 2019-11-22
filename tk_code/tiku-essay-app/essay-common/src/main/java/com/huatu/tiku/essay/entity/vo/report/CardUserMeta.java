package com.huatu.tiku.essay.entity.vo.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private String averageStr; //平均分(字符串)
    private String maxStr;  //最高分（字符串）
}
