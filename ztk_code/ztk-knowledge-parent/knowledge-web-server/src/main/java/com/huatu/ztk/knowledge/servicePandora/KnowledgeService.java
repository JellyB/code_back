package com.huatu.ztk.knowledge.servicePandora;

import com.huatu.tiku.entity.knowledge.Knowledge;
import com.huatu.ztk.commons.Module;
import com.huatu.ztk.knowledge.bean.QuestionPoint;
import service.BaseServiceHelper;

import java.util.List;

/**
 * Created by lijun on 2018/8/21
 */
public interface KnowledgeService extends BaseServiceHelper<Knowledge> {

    /**
     * 获取某个科目的顶级知识点信息
     * @param subjectId 科目ID
     */
    List<Module> findModule(int subjectId);

    /**
     * 获取某个知识点的详情
     * @param knowledgeId
     * @return
     */
    QuestionPoint findById(int knowledgeId);
}
