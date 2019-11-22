package com.huatu.ztk.backend.paper.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * Created by ht on 2016/12/23.
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class PaperBean {
    private int id; //试卷id
    private String name; //试卷名字
    private int year; //年份
    private int area;//所属区域
    private String areas;//所属区域(以逗号分隔)
    private String areaName;//区域名
    private int time;//答题时间,分钟
    private int score;//分数
    private int type;
    private int status;
    private int catgory;//科目
    private int createdBy;//创建人
    private Date createTime;//创建时间
    private List<ModuleBean> modules;
    private boolean recommend; //推荐
    private String createUser;//发布人
}
