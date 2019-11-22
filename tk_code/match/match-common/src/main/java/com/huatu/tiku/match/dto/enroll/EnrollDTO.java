package com.huatu.tiku.match.dto.enroll;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by huangqingpeng on 2019/1/10.
 */
@Data
public class EnrollDTO implements Serializable {

    private Integer userId;

    private Integer paperId;

    private Integer positionId;

    private Integer schoolId;

    private String schoolName;

    private Long enrollTime;

    private Long essayPaperId;
}
