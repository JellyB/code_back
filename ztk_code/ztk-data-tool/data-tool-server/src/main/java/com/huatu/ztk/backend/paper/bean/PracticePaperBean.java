package com.huatu.ztk.backend.paper.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by aatrox on 2017/3/6.
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class PracticePaperBean extends PaperBean {
    private long startTime;         //开始时间,毫秒
    private long endTime;           //结束时间，毫秒
    private long onlineTime;        //上线时间,毫秒
    private long offlineTime;       //下线时间,毫秒
    private int onStatus;//是否上线状态
    private int countAnswer;//参与答题人数
    private int lookParseTime;      //查看报告的时间，1：立即查看，2：考试结束后查看
    private String descrp;          //试卷说明
    private String url;             //试卷报名url
    private int hideFlag;//隐藏标记，1显示 0 隐藏
    private List<PracticeModuleBean> practiceModuleBeans;
    private int courseId; //解析课id
    private int tag;//标签
    private String courseInfo; //解析课说明
    private String instruction; //考试说明
    /**PC考试说明*/
    private String instructionPC;
//    private String instructionPC;
    /** 申论考试id */
    private long essayId;
}
