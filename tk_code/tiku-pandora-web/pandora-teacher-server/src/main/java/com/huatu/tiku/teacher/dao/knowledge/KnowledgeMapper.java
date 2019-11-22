package com.huatu.tiku.teacher.dao.knowledge;

import com.huatu.tiku.entity.knowledge.Knowledge;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 知识点
 *
 * @author zhouwei
 * @date 2018-04-23 14:30:04
 */
@Repository
public interface KnowledgeMapper extends Mapper<Knowledge> {

    /**
     * 查询属于某一科目下的所有知识点（不包含父节点自己的知识点）
     *
     * @param subjectIds
     * @return
     */
    List<Knowledge> selectKnowledgeBySubject(@Param("subjectIds") List<Long> subjectIds);

    /**
     * 查询科目下有每个知识点有多少个试题
     *
     * @param subjectId
     * @return
     */
    List<Map<String, Long>> countKnowledgeQuestionBySubject(@Param("subjectId") Long subjectId);

    /**
     * 根据知识点查询所对应的科目信息
     *
     * @param knowledgeIds 知识点ID
     * @return 科目信息
     * knowledgeId -> 知识点ID
     * subjectId -> 对应科目ID(所有ID拼接的字符串)
     * subjectName -> 对应科目名称(所有Name拼接的字符串)
     */
    List<HashMap<String, Object>> findSubjectInfoByKnowledge(@Param("knowledgeIds") List<Long> knowledgeIds);
}
