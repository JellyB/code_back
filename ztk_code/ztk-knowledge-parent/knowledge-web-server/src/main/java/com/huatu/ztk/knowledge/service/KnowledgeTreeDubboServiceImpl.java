package com.huatu.ztk.knowledge.service;

import com.huatu.ztk.knowledge.api.KnowledgeTreeDubboService;
import com.huatu.ztk.knowledge.api.QuestionPointDubboService;
import com.huatu.ztk.knowledge.bean.QuestionPoint;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import com.huatu.ztk.knowledge.util.QuestionPointUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by lijianying on 6/2/16.
 */
@Service
public class KnowledgeTreeDubboServiceImpl implements KnowledgeTreeDubboService {

    private static final Logger logger = LoggerFactory.getLogger(KnowledgeTreeDubboServiceImpl.class);

    @Autowired
    private QuestionPointDubboService questionPointDubboService;

    /**
     * 通过知识点id列表组装知识点数
     *
     * @param points 知识点列表
     * @return
     */
    @Override
    public List<QuestionPointTree> findByIds(List<Integer> points) {
        final List<QuestionPoint> questionPoints = questionPointDubboService.findBath(points);
        final List<QuestionPointTree> questionPointTrees = QuestionPointUtil.transform2Trees(questionPoints);
        return questionPointTrees;
    }
}
