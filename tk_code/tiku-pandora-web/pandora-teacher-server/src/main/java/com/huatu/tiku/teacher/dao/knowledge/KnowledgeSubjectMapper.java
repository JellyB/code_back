package com.huatu.tiku.teacher.dao.knowledge;

import com.huatu.tiku.entity.knowledge.Knowledge;
import com.huatu.tiku.entity.knowledge.KnowledgeSubject;
import com.huatu.tiku.entity.subject.Subject;
import com.huatu.tiku.teacher.dao.provider.knowledge.KnowledgeSubjectProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * Created by huangqp on 2018\5\24 0024.
 */
@Repository
public interface KnowledgeSubjectMapper extends Mapper<KnowledgeSubject> {

    @SelectProvider(type = KnowledgeSubjectProvider.class, method = "selectKnowledgeBySubjectId")
    List<Knowledge> selectKnowledgeBySubjectId(@Param("arg0") List<Subject> subjectIdList);

    @SelectProvider(type = KnowledgeSubjectProvider.class, method = "selectKnowledgeNameByQuestionId")
    List<String> selectKnowledgeNameByQuestionId(@Param("arg0") List<Long> questionIds);


    @SelectProvider(type = KnowledgeSubjectProvider.class, method = "updateKnowledgeIdByQuestionId")
    Integer updateKnowledgeIdByQuestionId(Long rightKnowledgeId, List<Long> questionIds);

}