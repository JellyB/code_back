package com.huatu.tiku.teacher.service.activity;

import java.util.List;

/**
 * Created by huangqingpeng on 2019/1/28.
 */
public interface TruePaperService {

    /**
     * 通过活动试卷ID处理试卷试题的知识点分布情况
     * @param activityId
     * @return
     */
    String handlerKnowledgeExcelById(long activityId);

    List<String> handlerKnowledgeExcelsByRange(int year, int subject, int activityType);
}
