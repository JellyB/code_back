package com.huatu.tiku.match.bo.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-01-09 下午2:26
 **/

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportListBo implements Serializable{
    private static final long serialVersionUID = 1L;
    private String name;
    private long practiceId;
    private int paperId;
    private long startTime;
    private int total;

    //课程相关信息
    private int courseId;
    private String courseName;

}
