package com.huatu.ztk.knowledge.servicePandora.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.huatu.tiku.entity.knowledge.Knowledge;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.Module;
import com.huatu.ztk.knowledge.bean.QuestionPoint;
import com.huatu.ztk.knowledge.common.PointStatus;
import com.huatu.ztk.knowledge.daoPandora.KnowledgeMapper;
import com.huatu.ztk.knowledge.servicePandora.KnowledgeService;
import com.huatu.ztk.knowledge.util.DebugCacheUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import service.impl.BaseServiceHelperImpl;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by lijun on 2018/8/21
 */
@Service
public class KnowledgeServiceImpl extends BaseServiceHelperImpl<Knowledge> implements KnowledgeService {

    public KnowledgeServiceImpl() {
        super(Knowledge.class);
    }
    private static final Logger logger = LoggerFactory.getLogger(KnowledgeServiceImpl.class);

    @Autowired
    private KnowledgeMapper mapper;

    //知识点缓存
    private static final Cache<Integer, QuestionPoint> questionPointCache =
            CacheBuilder.newBuilder()
                    .expireAfterWrite(3, TimeUnit.DAYS)//缓存时间
                    .maximumSize(1000)//最大1000
                    .build();

    @Override
    public List<Module> findModule(int subjectId) {
        List<Knowledge> knowledgeList = mapper.getFirstLevelBySubjectId(subjectId);
        List<Module> moduleList = knowledgeList.stream()
                .map(knowledge ->
                        Module.builder()
                                .id(knowledge.getId().intValue())
                                .name(knowledge.getName())
                                .build()
                )
                .collect(Collectors.toList());
        return moduleList;
    }

    @Override
    public QuestionPoint findById(int knowledgeId) {
        DebugCacheUtil.showCacheContent(questionPointCache,"questionPointCache");
        QuestionPoint questionPoint = questionPointCache.getIfPresent(knowledgeId);
        if (questionPoint == null) {
            questionPoint = findByMysql(knowledgeId);
            if (questionPoint != null) {
                logger.info("save knowledge point to db. data={}", JsonUtil.toJson(questionPoint));
                questionPointCache.put(knowledgeId, questionPoint);
            }
        }
        return questionPoint;
    }

    private QuestionPoint findByMysql(int knowledgeId) {

        List<HashMap<String, Object>> knowledgeInfoById = mapper.getKnowledgeInfoById(knowledgeId);
        if (CollectionUtils.isNotEmpty(knowledgeInfoById)) {
            HashMap<String, Object> map = knowledgeInfoById.get(0);
            QuestionPoint questionPoint = QuestionPoint.builder()
                    .id(MapUtils.getIntValue(map, "id", 0))
                    .name(MapUtils.getString(map, "name", ""))
                    .parent(MapUtils.getIntValue(map, "parentId"))
                    //新版本与旧版本 层级相差 1
                    .level(MapUtils.getIntValue(map, "level") - 1)
                    .qnumber(MapUtils.getIntValue(map, "questionNum", 0))
                    .status(PointStatus.AUDIT_SUCCESS)
                    .build();
            //填充子类的questionId
            Example example = Example.builder(Knowledge.class).build();
            example.and().andEqualTo("parentId", questionPoint.getId());
            List<Knowledge> knowledgeChildrenList = selectByExample(example);
            List<Integer> childrenIdList = knowledgeChildrenList.stream()
                    .map(Knowledge::getId)
                    .map(Long::intValue)
                    .collect(Collectors.toList());
            questionPoint.setChildren(childrenIdList);
            return questionPoint;
        }
        return null;

    }
}
