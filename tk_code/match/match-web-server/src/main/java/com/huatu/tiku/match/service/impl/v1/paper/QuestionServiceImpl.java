package com.huatu.tiku.match.service.impl.v1.paper;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.huatu.tiku.match.bo.paper.QuestionSimpleBo;
import com.huatu.tiku.match.dao.document.QuestionDao;
import com.huatu.tiku.match.service.v1.paper.PaperService;
import com.huatu.tiku.match.service.v1.paper.QuestionService;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.question.bean.Question;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by lijun on 2018/11/1
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class QuestionServiceImpl implements QuestionService {

    final QuestionDao questionDao;

    final PaperService paperService;

    /**
     * 用以在本机缓存试题信息
     */
    private final static Cache<Integer, Question> QUESTION_CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(45, TimeUnit.MINUTES)
            .maximumSize(500)
            .build();


    @Override
    public Question findQuestionCacheById(Integer questionId) {
        Question question = QUESTION_CACHE.getIfPresent(questionId);
        if (null != question) {
            return question;
        }
        Question mongoDBInfo = questionDao.findQuestionById(questionId);
        if (null != mongoDBInfo) {
            QUESTION_CACHE.put(questionId, mongoDBInfo);
            return QUESTION_CACHE.getIfPresent(questionId);
        }
        return null;
    }

    @Override
    public List<Question> findQuestionCacheByIds(final List<Integer> questionIds) {
        if(CollectionUtils.isEmpty(questionIds)){
            return Lists.newArrayList();
        }
        StopWatch stopWatch = new StopWatch("findQuestionCacheByIds");
        stopWatch.start("CacheQuery");
        List<Question> cacheList = questionIds.stream()
                .map(QUESTION_CACHE::getIfPresent)
                .filter(question -> null != question)
                .collect(Collectors.toList());
        stopWatch.stop();
        //如果数据不全，从数据库补充
        if (CollectionUtils.isEmpty(cacheList) || cacheList.size() < questionIds.size()) {
            final Set<Integer> idSet = cacheList.stream()
                    .map(Question::getId)
                    .collect(Collectors.toSet());
            List<Integer> notExistCacheIdList = questionIds.stream()
                    .filter(id -> !idSet.contains(id))
                    .collect(Collectors.toList());
            stopWatch.start("findQuestionById");
            List<Question> dbQuestionList = questionDao.findQuestionById(notExistCacheIdList);
            dbQuestionList.forEach(question -> QUESTION_CACHE.put(question.getId(), question));
            cacheList.addAll(dbQuestionList);
            stopWatch.stop();
        }

        //保证和传入ID 序列一致
        cacheList.sort(Comparator.comparing(question -> questionIds.indexOf(question.getId())));
        log.info("findQuestionCacheByIds stopWatch:{}",stopWatch.prettyPrint());
        return cacheList;
    }

    @Override
    public List<QuestionSimpleBo> findQuestionSimpleBoById(final List<Integer> questionIdList) {
        if (CollectionUtils.isEmpty(questionIdList)) {
            return Lists.newArrayList();
        }
        List<Question> cacheList = findQuestionCacheByIds(questionIdList);
        List<QuestionSimpleBo> collect = cacheList.stream()
                .map(QuestionUtil::transQuestionInfoToSimpleBo)
                .collect(Collectors.toList());
        return collect;
    }


    @Override
    public List<QuestionSimpleBo> findQuestionSimpleBoByPaperId(int paperId) {
        List<Integer> paperQuestionIdList = paperService.getPaperQuestionIdList(paperId);
        if (CollectionUtils.isNotEmpty(paperQuestionIdList)) {
            final Paper paper = paperService.findPaperCacheById(paperId);
            List<QuestionSimpleBo> questionSimpleBoList = findQuestionSimpleBoById(paperQuestionIdList);
            questionSimpleBoList.forEach(questionSimpleBo -> QuestionUtil.buildModuleInfo(questionSimpleBo, paper));
            return questionSimpleBoList;
        }
        return Lists.newArrayList();
    }
}
