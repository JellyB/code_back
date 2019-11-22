package com.huatu.tiku.essay.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by huangqp on 2017\12\11 0011.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPaperWithQuestionVO {
    //id
    private Long id;
    /* 试卷名称 */
    private String name;
    /** 试题年份 **/
    private String paperYear;
    /** 试题日期 **/
    private String paperDate;
    /** 地区id **/
    private long areaId;
    /** 地区名称 **/
    private String areaName;
    /** 下级地区id **/
    private long subAreaId;
    /** 下级地区名称 **/
    private String subAreaName;
    /** 真题 1  模考题 0 **/
    private int type;
    /* 答题限时 */
    private int limitTime;
    /* 得分 */
    private double score;
    private Integer status;
    private Integer bizStatus;
    List<AdminQuestionVO> questions;


    //tag
    private int tag;
    //是否是联合模考(1 联合模考 2申论模考)
    private int mockType;
    //解析课介绍
    private String courseInfo;
    //解析课id
    private int courseId;
    //考试说明
    private String instruction;
    private String instructionPC;
}
