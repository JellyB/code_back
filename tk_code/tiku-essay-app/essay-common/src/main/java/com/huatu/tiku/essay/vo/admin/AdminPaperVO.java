package com.huatu.tiku.essay.vo.admin;

import com.huatu.tiku.essay.constant.status.EssayPaperBaseConstant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by x6 on 2017/12/10.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class  AdminPaperVO {

    //id
    private long paperId;

    //名称
    private String name;
    //年份
    private String  paperYear;
    //日期
    private String paperDate;
    //地区(例如：安徽)
    private long areaId;
    //地区名称
    private String areaName;
    //试题类型(例如：安徽B卷)
    private long typeId;
    //类型名称
    private String typeName;

    //答题限时
    private int limitTime;
    //总分
    private double score;

    private int status = EssayPaperBaseConstant.EssayPaperStatusEnum.UN_CHECK.getStatus();
    private  int bizStatus = EssayPaperBaseConstant.EssayPaperBizStatusEnum.OFFLINE.getBizStatus();


    //试卷类型   0 模考题  1真题
    private int type;




}
