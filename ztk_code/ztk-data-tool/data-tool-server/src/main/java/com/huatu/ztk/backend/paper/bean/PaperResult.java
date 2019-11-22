package com.huatu.ztk.backend.paper.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 模考估分结果
 * Created by linkang on 3/5/17.
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class PaperResult {
    private int totalCount; //总答题人数
    private int eightyHighCount; //>80分人数
    private int sixtyToEightyCount; //60-80分人数
    private int sixtyLowCount; //<60分人数
    private double maxScore; //最高分
    private double minScore; //最低分
    private double avgScore;  //平均分
    private int zeroCount; //0分人数
    private int qcount; //试卷试题总数
}
