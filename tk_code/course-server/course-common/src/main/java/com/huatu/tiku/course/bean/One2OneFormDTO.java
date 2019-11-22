package com.huatu.tiku.course.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author hanchao
 * @date 2017/9/22 13:53
 */
@Data
public class One2OneFormDTO implements Serializable{
    private static final long serialVersionUID = 1L;

    @JsonProperty("Age")
    private Integer Age;
    @JsonProperty("ApplyJobs")
    private String ApplyJobs;
    @JsonProperty("ApplyNum")
    private Integer ApplyNum;
    @JsonProperty("Edu")
    private int Edu;
    @JsonProperty("ExamExperience")
    private String ExamExperience;
    @JsonProperty("Examtime")
    private String Examtime;
    @JsonProperty("NetClassCategory")
    private String NetClassCategory;
    @JsonProperty("NetClassName")
    private String NetClassName;
    @JsonProperty("NetClassType")
    private int NetClassType;
    @JsonProperty("QQ")
    private String QQ;
    @JsonProperty("Sex")
    private int Sex;
    @JsonProperty("Telephone")
    private String Telephone;
    @JsonProperty("UserBz")
    private String UserBz;
    @JsonProperty("UserReName")
    private String UserReName;
    @JsonProperty("ViewRatio")
    private String ViewRatio;
    @JsonProperty("ViewRate")
    private String ViewRate;
    @JsonProperty("OrderNum")
    private String OrderNum;
    @JsonProperty("rid")
    private String rid;
    @JsonProperty("score")
    private String score;
}
