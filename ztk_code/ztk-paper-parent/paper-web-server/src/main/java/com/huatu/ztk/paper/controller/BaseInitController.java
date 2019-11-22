package com.huatu.ztk.paper.controller;

import com.google.common.collect.Queues;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.common.AnswerCardType;
import com.huatu.ztk.paper.service.PracticeCardService;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by shaojieyue
 * Created time 2016-05-12 18:00
 */
public abstract class BaseInitController {

    @Autowired
    protected QuestionDubboService questionDubboService;


    @Autowired
    protected PracticeCardService practiceCardService;

    ThreadPoolExecutor executor = new ThreadPoolExecutor(20, 60, 2000, TimeUnit.HOURS,
            new ArrayBlockingQueue<Runnable>(200000));

    public static final Map<String, String> answerMap = new HashMap();
    public static final Map<Integer, Integer> cardTypeMap = new HashMap();
    public static final Map<String, Integer> mobileTypeMap = new HashMap();
    static {
        cardTypeMap.put(1, AnswerCardType.SMART_PAPER);
        cardTypeMap.put(2,AnswerCardType.CUSTOMIZE_PAPER);
        cardTypeMap.put(3,AnswerCardType.TRUE_PAPER);
        cardTypeMap.put(4,AnswerCardType.MOCK_PAPER);
        cardTypeMap.put(5,AnswerCardType.ARENA_PAPER);//《国家行测》-竞技练习-20131018 只有不到10条记录
        cardTypeMap.put(6,AnswerCardType.WRONG_PAPER);//《国家行测》-错题重练-2016050924319315
        cardTypeMap.put(7,AnswerCardType.MOCK_PAPER);//2016安徽事业单位公共基础一模拟卷
        cardTypeMap.put(8,AnswerCardType.MOCK_PAPER);//2016上半年联考判断推理练习题的副本
        cardTypeMap.put(9,AnswerCardType.MOCK_PAPER);//砖超联赛11月05号海选-18346267
        cardTypeMap.put(10,AnswerCardType.MOCK_PAPER);//砖超联赛
        cardTypeMap.put(51,AnswerCardType.SMART_PAPER); //《国家行测》-智能推送(竞技)-2016050924319691
        cardTypeMap.put(52,AnswerCardType.CUSTOMIZE_PAPER); //竞技赛场->考点直击(竞技)
        cardTypeMap.put(53,AnswerCardType.ARENA_PAPER); //砖超竞技
        cardTypeMap.put(54,AnswerCardType.ARENA_PAPER); //砖超竞技
        cardTypeMap.put(55,AnswerCardType.ARENA_PAPER); //砖超竞技

        answerMap.put("A", "1");
        answerMap.put("B", "2");
        answerMap.put("C", "3");
        answerMap.put("D", "4");
        answerMap.put("E", "5");
        answerMap.put("F", "6");
        answerMap.put("G", "7");
        answerMap.put("H", "8");

        mobileTypeMap.put("竞技练习",AnswerCardType.ARENA_PAPER);
        mobileTypeMap.put("每日特训",AnswerCardType.SMART_PAPER);
        mobileTypeMap.put("考点直击",AnswerCardType.CUSTOMIZE_PAPER);
        mobileTypeMap.put("我猜你练",AnswerCardType.CUSTOMIZE_PAPER);
        mobileTypeMap.put("错题库",AnswerCardType.WRONG_PAPER);
        mobileTypeMap.put("收藏夹", AnswerCardType.SMART_PAPER);
        mobileTypeMap.put("真题演练",AnswerCardType.TRUE_PAPER);
        mobileTypeMap.put("砖超联赛-海选",AnswerCardType.MOCK_PAPER);
        mobileTypeMap.put("专项练习",AnswerCardType.CUSTOMIZE_PAPER);
        mobileTypeMap.put("微信答题",AnswerCardType.CUSTOMIZE_PAPER);
        mobileTypeMap.put("精准估分",AnswerCardType.CUSTOMIZE_PAPER);

    }

    public static final Map<Integer,String> knowledge = new HashMap();
    static {
        knowledge.put(392,"常识判断");
        knowledge.put(435,"言语理解与表达");
        knowledge.put(482,"数量关系");
        knowledge.put(642,"判断推理");
        knowledge.put(754,"资料分析");
    }

    public static final ConcurrentMap<Integer,GenericQuestion> question_map = new ConcurrentHashMap<>();

    protected void initQuestions(){
        System.out.println("start query all questions.");
        int step = 50;
        for (int i = 30725; i <136100; i = i + step) {
            List<Integer> ids = new ArrayList<>();
            for (int j = i; j < i+step; j++) {
                ids.add(j);
            }
            //批量请求缺少的
            List<Question> questions  = questionDubboService.findBath(ids);
            for (Question question : questions) {
                if (question == null || !(question instanceof GenericQuestion)) {
                    continue;
                }

                if (question != null) {
                    question_map.put(question.getId(),(GenericQuestion)question);
                }
            }
        }

        System.out.println("end query all questions. init finish. questionCount="+question_map.size());

    }

}
