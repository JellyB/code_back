package com.huatu.ztk.paper.bean;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class MatchVO implements Serializable {
    //用户答题状态
    private int status;
    private String name; //大赛名称
    private int time;//答题时间
    private int qcount;//题量
    private int paperId; //试卷id
    private int tag;//标签  用来区别2018国考/省考
    private int subject;//考试科目
    private PaperUserMeta userMeta;//答题统计数据
}
