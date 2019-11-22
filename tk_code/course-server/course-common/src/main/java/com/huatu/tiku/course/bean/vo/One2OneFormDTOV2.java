package com.huatu.tiku.course.bean.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author biguodong
 * @date 2019/06/12 13:53
 */
@Data
public class One2OneFormDTOV2 implements Serializable{
    private static final long serialVersionUID = 1L;

    @JsonProperty("Age")
    private Integer Age;
    @JsonProperty("ApplyJobs")
    private String ApplyJobs;
    @JsonProperty("ApplyNum")
    private Integer ApplyNum;
    @JsonProperty("Edu")
    @NotNull(message = "Edu 不能为空")
    private int Edu;
    @JsonProperty("ExamExperience")
    private String ExamExperience;
    @JsonProperty("Examtime")
    private String Examtime;
    @JsonProperty("NetClassCategory")
    private String NetClassCategory;
    @JsonProperty("NetClassCategoryId")
    @NotNull(message = "NetClassCategoryId 不能为空")
    private Long NetClassCategoryId;
    @JsonProperty("NetClassName")
    private String NetClassName;
    @JsonProperty("NetClassType")
    private int NetClassType;
    @JsonProperty("OrderNum")
    @NotNull(message = "OrderNum 不能为空")
    private String OrderNum;
    @JsonProperty("QQ")
    private String QQ;
    @JsonProperty("Sex")
    private int Sex;
    @JsonProperty("Telephone")
    @NotNull(message = "Telephone 不能为空")
    private String Telephone;
    @JsonProperty("UserBz")
    private String UserBz;
    @JsonProperty("UserID")
    private String UserID;
    @JsonProperty("UserReName")
    @NotNull(message = "UserReName 不能为空")
    private String UserReName;
    @JsonProperty("ViewRatio")
    private String ViewRatio;
    @JsonProperty("area")
    private String area;
    @JsonProperty("classTime")
    private String classTime;
    @JsonProperty("major")
    private String major;
    @JsonProperty("orderID")
    private String orderID;
    @JsonProperty("renewRemark")
    private String renewRemark;
    @JsonProperty("score")
    private String score;
    @JsonProperty("stage")
    private String stage;
    @JsonProperty("subject")
    private String subject;
}
