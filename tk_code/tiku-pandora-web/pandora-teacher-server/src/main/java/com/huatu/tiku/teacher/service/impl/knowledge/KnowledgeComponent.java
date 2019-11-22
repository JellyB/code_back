package com.huatu.tiku.teacher.service.impl.knowledge;

import com.google.common.collect.Lists;
import com.huatu.tiku.entity.knowledge.Knowledge;
import com.huatu.tiku.teacher.service.knowledge.KnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 知识点工具类
 * Created by lijun on 2018/12/13
 */
@Component
public class KnowledgeComponent {

    @Autowired
    KnowledgeService knowledgeService;

    @Resource(name = "redisTemplate")
    private HashOperations<String, String, Knowledge> hashOperations;

    private final static String KNOWLEDGE_CACHE = "_knowledge:cache:_";

    /**
     * 从当前节点获取 到顶级节点的 名称
     */
    public List<Knowledge> getParentUtilRoot(Long id) {
        return getDataFromNode(id, Knowledge::getParentId);
    }

    public void saveCacheInfo(Knowledge knowledge) {
        if (null != knowledge) {
            hashOperations.put(KNOWLEDGE_CACHE, String.valueOf(knowledge.getId()), knowledge);
        }
    }

    public Knowledge getCacheKnowledgeInfo(Long id) {
        Knowledge knowledge = hashOperations.get(KNOWLEDGE_CACHE, String.valueOf(id));
        if (null == knowledge) {
            Knowledge knowledgeDBInfo = knowledgeService.selectByPrimaryKey(id);
            if (null != knowledgeDBInfo) {
                saveCacheInfo(knowledgeDBInfo);
            }
            return hashOperations.get(KNOWLEDGE_CACHE, String.valueOf(id));
        }
        return knowledge;
    }

    public void deleteCacheInfo(Long id) {
        hashOperations.delete(KNOWLEDGE_CACHE, String.valueOf(id));
    }

    /**
     * 获取所有的层级信息
     *
     * @param id            当前ID
     * @param getNextNodeId 向上 or 向下
     * @return 所有的层级信息
     */
    private List getDataFromNode(final Long id, Function<Knowledge, Long> getNextNodeId) {
        final List<Knowledge> result = Lists.newArrayList();
        Consumer<Long>[] consumers = new Consumer[1];
        consumers[0] = (currentId) -> {
            Knowledge currentKnowledgeInfo = getCacheKnowledgeInfo(currentId);
            if (null != currentKnowledgeInfo) {
                result.add(currentKnowledgeInfo);
                if (currentKnowledgeInfo.getParentId() != 0) {
                    consumers[0].accept(getNextNodeId.apply(currentKnowledgeInfo));
                }
            }
        };
        consumers[0].accept(id);
        return result;
    }

}
