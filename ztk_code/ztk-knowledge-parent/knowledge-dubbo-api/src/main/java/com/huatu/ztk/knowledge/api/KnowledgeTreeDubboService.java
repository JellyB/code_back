package com.huatu.ztk.knowledge.api;

import com.huatu.ztk.knowledge.bean.QuestionPointTree;

import java.util.List;

/**
 * 知识点树相关Dubbo接口
 * Created by lijianying on 6/2/16.
 */
public interface KnowledgeTreeDubboService {

    /**
     * 通过知识点id列表组装知识点数
     * @param points 知识点列表
     * @return
     */
    public List<QuestionPointTree> findByIds(List<Integer> points);
}
