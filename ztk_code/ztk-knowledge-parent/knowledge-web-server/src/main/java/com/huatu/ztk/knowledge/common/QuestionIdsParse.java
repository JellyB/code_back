package com.huatu.ztk.knowledge.common;

import com.google.common.collect.HashMultimap;
import com.huatu.ztk.knowledge.api.QuestionPointDubboService;
import com.huatu.ztk.knowledge.bean.Module;
import com.huatu.ztk.knowledge.bean.QuestionPoint;
import com.huatu.ztk.knowledge.bean.QuestionStrategy;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 知识点,试题数据结构解析
 * Created by shaojieyue
 * Created time 2016-05-20 09:11
 */

@Component
public class QuestionIdsParse {
    private static final Logger logger = LoggerFactory.getLogger(QuestionIdsParse.class);

    @Autowired
    private QuestionPointDubboService questionPointDubboService;


    /**
     * 试题和难度的分割
     */
    public static final String QUESTION_ID_DIFFICULT = ":";
    /**
     * 试题列表和知识点的分割
     */
    public static final String DIFFICULT_POINT = "@";
    /**
     * 试题于试题之间的分割
     */
    public static final String QUESTION_ID_QUESTION_ID = ",";

    /**
     * 拼接试题核心数据,组装出来的set value结构为 试题id:难度系数
     * @param question
     * @param stringBuilder
     * @return
     */
    private static final StringBuilder combine(Question question, StringBuilder stringBuilder){
        GenericQuestion genericQuestion = (GenericQuestion)question;
        stringBuilder.append(genericQuestion.getId())
                .append(QUESTION_ID_DIFFICULT).append(genericQuestion.getDifficult());
        return stringBuilder;
    }

    /**
     * 拼接试题核心数据,组装出来的set value结构为 试题id:难度系数@模块id
     * @param question
     * @return
     */
    public  final StringBuilder combine(Question question){
        GenericQuestion genericQuestion = (GenericQuestion)question;
        return combine(question,new StringBuilder()).append(DIFFICULT_POINT).append(genericQuestion.getPoints().get(0));
    }

    /**
     * 复合题 组装出来的set value结构为 试题id:难度系数,..N..试题idN:难度系数@模块id
     * @param questionList
     * @return
     */
    public  final StringBuilder combine(List<Question> questionList){
        StringBuilder stringBuilder = new StringBuilder();
        if (CollectionUtils.isEmpty(questionList)) {
            return stringBuilder;
        }
        for (Question question : questionList) {
            combine(question,stringBuilder).append(QUESTION_ID_QUESTION_ID);
        }

        return stringBuilder.deleteCharAt(stringBuilder.length()-1);
    }

    /**
     * 将试题数据转换为QuestionStrategy
     * @param sets 试题列表
     * @param size 需要试题的个数
     * @return
     */
    public  final QuestionStrategy parse(Collection<String> sets, int size){
        //模块试题对应关系
        final HashMultimap<Integer, Integer> multimap = HashMultimap.create();
        int difficultySum = 0;
        int questionCount = 0;
        for (String set : sets) {
            int index = set.indexOf(DIFFICULT_POINT);
            int modelId = Integer.valueOf(set.substring(index+1));

            //数据结构试题id:难度系数,..N..试题id:难度系数
            String[] array = set.substring(0,index).split(QUESTION_ID_QUESTION_ID);

            for (String str : array) {
                //数据结构试题id:难度系数
                String[] arr = str.split(QUESTION_ID_DIFFICULT);
                int questionId = Integer.valueOf(arr[0]);
                //true:添加成功,false:说明已经存在
                final boolean success = multimap.put(modelId, questionId);
                if (success) {//添加成功,试题量+1
                    questionCount ++;
                    //累计难度
                    difficultySum = difficultySum + Integer.valueOf(arr[1]);
                }
                //试题已经取够
                if (questionCount == size) {
                    break;
                }
            }
            //试题已经取够
            if (questionCount == size) {
                break;
            }
        }
        
        List<Module> modules = new ArrayList<>();
        List<Integer> questions = new ArrayList<>();
        //遍历多值map,组装QuestionStrategy
        for (Integer moduleId : multimap.keySet()) {
            final QuestionPoint point = questionPointDubboService.findById(moduleId);
            final Set<Integer> ids = multimap.get(moduleId);
            final Module module = Module.builder().category(point.getId())
                    .name(point.getName())
                    .qcount(ids.size()).build();
            modules.add(module);
            questions.addAll(ids);
        }
        //计算难度,保证一位小数点
        BigDecimal difficulty = new BigDecimal(difficultySum).divide(new BigDecimal(questionCount),1,BigDecimal.ROUND_HALF_UP);
        final QuestionStrategy questionStrategy = QuestionStrategy.builder().modules(modules)
                .questions(questions)
                .difficulty(difficulty.doubleValue()).build();
        return questionStrategy;
    }
}
