package com.huatu.ztk.knowledge.daoPandora;

import com.huatu.tiku.entity.knowledge.Knowledge;
import com.huatu.ztk.knowledge.daoPandora.provider.KnowledgeProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.HashMap;
import java.util.List;

/**
 * Created by lijun on 2018/8/21
 */
@Repository
public interface KnowledgeMapper extends Mapper<Knowledge> {

    /**
     * 获取某个科目下的一级知识点信息
     *
     * @param subjectId 科目ID
     * @return
     */
    @SelectProvider(type = KnowledgeProvider.class, method = "getFirstLevelBySubjectId")
    List<Knowledge> getFirstLevelBySubjectId(@Param("arg0") int subjectId);

    /**
     * 查询一个知识点详情 - 附带试题数量
     *
     * @param knowledgeId 科目ID
     * @return
     */
    @SelectProvider(type = KnowledgeProvider.class, method = "getKnowledgeInfoById")
    List<HashMap<String, Object>> getKnowledgeInfoById(@Param("arg0") int knowledgeId);


    /**
     * 查询某个科目的用户知识点ID
     */
    @SelectProvider(type = KnowledgeProvider.class, method = "getKnowledgeBySubjectId")
    List<HashMap<String, Object>> getKnowledgeBySubjectId(String knowledgeIds,
                                                          int subjectId);
}
