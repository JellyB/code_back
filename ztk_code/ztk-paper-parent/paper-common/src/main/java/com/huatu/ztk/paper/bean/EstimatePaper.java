package com.huatu.ztk.paper.bean;

import lombok.Data;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;

/**
 * 模考估分试卷
 * Created by shaojieyue
 * Created time 2016-07-23 16:54
 */
@Data
public class EstimatePaper extends Paper implements Serializable {
    private static final long serialVersionUID = 1L;

    private long startTime;         //开始时间,毫秒
    private long endTime;           //结束时间，毫秒
    private long onlineTime;        //上线时间,毫秒
    private long offlineTime;       //下线时间,毫秒
    private int lookParseTime;      //查看报告的时间，1：立即查看，2：考试结束后查看
    private String descrp;          //试卷说明
    private String url;             //试卷报名url
    private int hideFlag;           //隐藏标记，1 隐藏,0显示

    private int courseId;       //解析课程ID
    private String courseName;   //解析课程名称
    private String courseInfo;      //解析课程说明
    private String pointsName;      //涉及到的底层知识点名称
    @Transient
    private String iconUrl;             //活动图标
    @Transient
    private int takePartInCount;         //模考大赛参加人数
    private int startTimeIsEffective;    //在配置的考试时间内,是否生效(阶段测试使用)(0 否;1 是)
    private int formativeCourseId;       //阶段测试绑定的课程ID
    private int syllabusId;              //课程大纲ID
}
