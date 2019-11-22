package com.huatu.tiku.essay.entity.vo.report;

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
    private long paperId;
    private long essayPaperId;
    private long startTime;
    private int total;
    private int flag;	//1只有行测2只有申论3申论行测都有
    private int courseId;
    private String courseInfo;
}
