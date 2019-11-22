package com.huatu.ztk.paper.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * Created by linkang on 17-7-14.
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
@Document(collection = "ztk_match")
public class Match implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private int paperId; //试卷id
    private String name; //大赛名称

    private String timeInfo; //时间信息
    private String courseInfo; //课程信息

    private int courseId; //课程id
    private String instruction; //考试说明
    private String instructionPC; //考试说明
    private int tag;//标签  用来区别2018国考/省考
    private int subject;//考试科目

    private long startTime; //考试开始时间
    private long endTime; //结束时间
    private int status; //模考大赛状态
    private long essayPaperId;  //申论模考大赛试卷id
    private long essayStartTime;  //申论模考大赛开始时间
    private long essayEndTime;  //申论模考大赛结束时间
    @Transient
    private int flag;  //1只有行测报告2只有申论报告3行测申论报告都有
    @Transient
    private int stage;  //1表示行测模考大赛阶段2表示申论模考大赛阶段
    @Transient
    private int enrollCount; //总报名人数
    @Transient
    private int enrollFlag; //报名方式 0选择地区报名1无地区报名
    @Transient
    private MatchUserMeta userMeta; //用户信息
    @Transient
    private String iconUrl; //模考大赛大礼包链接
}
