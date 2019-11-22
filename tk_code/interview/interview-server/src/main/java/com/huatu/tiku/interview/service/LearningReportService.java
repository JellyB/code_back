package com.huatu.tiku.interview.service;

import com.huatu.tiku.interview.entity.result.Result;
import com.huatu.tiku.interview.entity.template.TemplateMsgResult;

import java.util.List;

/**
 * Created by x6 on 2018/1/17.
 * 学习报告相关接口
 */
public interface LearningReportService {
    Result dailyReport();


    TemplateMsgResult pushDailyReport(String openId,String date);


    Object detail(String openId, String date,int type);

    List<String> date(String openId);
}
