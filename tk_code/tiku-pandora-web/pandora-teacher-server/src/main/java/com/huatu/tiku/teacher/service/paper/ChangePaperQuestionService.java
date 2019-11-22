package com.huatu.tiku.teacher.service.paper;

import com.huatu.tiku.entity.teacher.PaperActivity;
import com.huatu.tiku.service.BaseService;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/4/16
 * @描述 新旧试题替换绑定关系
 */
public interface ChangePaperQuestionService extends BaseService<PaperActivity> {


    /**
     * @param oldQuestionId 旧试题ID
     * @param newQuestionId 新试题ID
     */
    void changePaperQueBindRelation(Long oldQuestionId, Long newQuestionId);
}
