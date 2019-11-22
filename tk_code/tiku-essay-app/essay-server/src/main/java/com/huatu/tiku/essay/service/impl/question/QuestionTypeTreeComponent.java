package com.huatu.tiku.essay.service.impl.question;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.huatu.tiku.essay.service.EssaySimilarQuestionService;
import com.huatu.tiku.essay.vo.resp.EssayQuestionTypeVO;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by lijun on 2018/10/26
 */
@Component
public class QuestionTypeTreeComponent {

    @Autowired
    private EssaySimilarQuestionService essaySimilarQuestionService;

    private static final Cache<String, LinkedList<EssayQuestionTypeVO>> cache = CacheBuilder.newBuilder()
            .maximumSize(2)
            .expireAfterWrite(300, TimeUnit.MINUTES)
            .build();

    private static final String CACHE_KEY = "_CACHE_KEY_ESSAY_QUESTION_TREE";

    /**
     * 查询科目树 从当前节点 递归至最下级节点
     */
    public List<EssayQuestionTypeVO> getQuestionTypeTree(long parentId) {
        LinkedList<EssayQuestionTypeVO> questionTypeTree = cache.getIfPresent(CACHE_KEY);
        //1.查询所有题目类型(走缓存)
        if (CollectionUtils.isEmpty(questionTypeTree)) {
            LinkedList<EssayQuestionTypeVO> questionType = (LinkedList) essaySimilarQuestionService.findQuestionType();
            cache.put(CACHE_KEY, questionType);
            questionTypeTree = cache.getIfPresent(CACHE_KEY);
        }
        //2.根据pid取出所有下级id
        List<EssayQuestionTypeVO> questionTypeList = new LinkedList<>();
        for (EssayQuestionTypeVO questionTypeVO : questionTypeTree) {
            if (questionTypeVO.getId() == parentId || questionTypeVO.getPid() == parentId) {
                questionTypeList.add(questionTypeVO);
            }
        }
        return questionTypeList;
    }

    /**
     * 查询科目树 从当前节点 递归至最下级节点
     */
    public List<Integer> getQuestionTypeTreeAndReturnId(long parentId) {
        List<EssayQuestionTypeVO> questionTypeTreeList = getQuestionTypeTree(parentId);
        if (CollectionUtils.isEmpty(questionTypeTreeList)) {
            return Lists.newArrayList();
        }
        List<Integer> questionTypeVoIdList = questionTypeTreeList.stream()
                .map(EssayQuestionTypeVO::getId)
                .map(Long::intValue)
                .collect(Collectors.toList());
        return questionTypeVoIdList;
    }

}
