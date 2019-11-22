package com.huatu.tiku.teacher.service.knowledge;

/**
 * 同步原有知识点 到新表中
 * Created by huangqingpeng on 2018/8/24.
 */
public interface SyncKnowledgeService {
    /**
     * 同步现有的subject中的knowledge 到新表中
     * @return
     */
    Object syncKnowledge();
}
