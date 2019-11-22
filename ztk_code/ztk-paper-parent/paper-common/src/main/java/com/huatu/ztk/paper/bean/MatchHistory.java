package com.huatu.ztk.paper.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class MatchHistory implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private long practiceId;
    private long startTime;
    private int total;
    /**
     * 申论试卷id(联合考试用)
     */
    private long essayPaperId;
    /**
     * 行测试卷id（新版本模考大赛查询报告用）
     */
    private int paperId;
    /**
     * 1只有行测报告2只有申论报告3行测报告申论报告都有
     */
    private int flag;
}
