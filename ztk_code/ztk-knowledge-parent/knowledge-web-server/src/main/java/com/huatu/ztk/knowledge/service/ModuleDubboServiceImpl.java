package com.huatu.ztk.knowledge.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.huatu.ztk.commons.SubjectType;
import com.huatu.ztk.knowledge.api.ModuleDubboService;
import com.huatu.ztk.knowledge.bean.Module;
import com.huatu.ztk.knowledge.bean.QuestionPoint;
import com.huatu.ztk.knowledge.common.QuestionPointLevel;
import com.huatu.ztk.knowledge.util.DebugCacheUtil;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 模块dubbo服务实现
 * Created by shaojieyue
 * Created time 2016-05-19 13:35
 */

@Service
public class ModuleDubboServiceImpl implements ModuleDubboService {
    private static final Logger logger = LoggerFactory.getLogger(ModuleDubboServiceImpl.class);


    @Autowired
    private PoxyUtilService poxyUtilService;

    /**
     * 科目下的一级知识点缓存数据
     */
    private static final Cache<Integer,List<com.huatu.ztk.commons.Module>> subjectModulesCache =
            CacheBuilder.newBuilder()
                    .expireAfterWrite(10, TimeUnit.DAYS)//缓存时间
                    .maximumSize(100)
                    .build();

    @Override
    public Module findByPointId(int pointId) {
        QuestionPoint questionPoint = poxyUtilService.getKnowledgeService().findById(pointId);
        Module module = null;
        //3级的,则取所属两级知识点
        if (questionPoint != null && questionPoint.getLevel() == QuestionPointLevel.LEVEL_THREE) {
            questionPoint = poxyUtilService.getKnowledgeService().findById(questionPoint.getParent());
        }
        //2级的,则取所属一级知识点
        if (questionPoint != null && questionPoint.getLevel() == QuestionPointLevel.LEVEL_TWO) {
            questionPoint = poxyUtilService.getKnowledgeService().findById(questionPoint.getParent());
        }
        if (questionPoint != null) {
            module = Module.builder()
                    .category(questionPoint.getId())
                    .name(questionPoint.getName())
                    .build();
        }
        return module;
    }


    @Override
    public List<com.huatu.ztk.commons.Module> findSubjectModules(int subject) {
        //事业单位行测的顶级知识点,与公务员行测相同
        if (subject == SubjectType.SYDW_XINGCE) {
            subject = SubjectType.GWY_XINGCE;
        }
        DebugCacheUtil.showCacheContent(subjectModulesCache, "subjectModulesCache");
        List<com.huatu.ztk.commons.Module> modules = subjectModulesCache.getIfPresent(subject);
        if (CollectionUtils.isEmpty(modules)) {
            modules = poxyUtilService.getKnowledgeService().findModule(subject);
            subjectModulesCache.put(subject, modules);
        }
        return modules;
    }

}
