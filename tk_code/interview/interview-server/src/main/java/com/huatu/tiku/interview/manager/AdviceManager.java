package com.huatu.tiku.interview.manager;

import com.huatu.tiku.interview.entity.po.LearningAdvice;
import com.huatu.tiku.interview.repository.LearningAdviceRepository;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * Created by x6 on 2018/4/17.
 */
public class AdviceManager {

    /**
     * 根据成绩查询建议
     * @param score
     * @param type
     * @return
     */
    public static String getAdvice(Double score,Integer type, LearningAdviceRepository learningAdviceRepository){
        int level = 0;
        String advice = "";
        //根据成绩判断分数等级
        if(score >= 0 && score <= 2){
            level = 3;
        }else if(score > 2 && score <= 4){
            level = 2;
        }else if(score > 4 && score <= 5){
            level = 1;
        }

        List<LearningAdvice> adviceList = learningAdviceRepository.findByTypeAndLevel(type, level);
        if(CollectionUtils.isNotEmpty(adviceList)){
            advice = adviceList.get(0).getContent();
        }
        return advice;
    }
}
