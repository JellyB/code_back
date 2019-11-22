package com.huatu.ztk.paper.service;

import com.google.common.collect.Lists;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.Question;
import com.sun.media.sound.SoftTuning;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by shaojieyue
 * Created time 2016-07-08 15:49
 */
public class QuestionServiceTest extends BaseTest{

    @Autowired
    private QuestionDubboService questionDubboService;


    @Test
    public void aa(){
        final Question question = questionDubboService.findById(55420);
        System.out.println(JsonUtil.toJson(question));
    }



}
