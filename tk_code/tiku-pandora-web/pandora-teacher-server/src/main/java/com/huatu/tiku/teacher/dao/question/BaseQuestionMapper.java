package com.huatu.tiku.teacher.dao.question;

import com.huatu.tiku.entity.question.BaseQuestion;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

/**
 * Created by huangqp on 2018\4\15 0015.
 */
@Repository
public interface BaseQuestionMapper extends Mapper<BaseQuestion> {

    /**
     * 查询试题，复用数据及试题的copFlag属性
     *
     * @param id
     * @return key contains questionId,subject,flag,duplicateId
     */
    Map findWithDuplicateById(@Param("questionId") Long id);

    /**
     * 根据复用id，查询相关联的试题，以及试题的科目id,copFlag
     *
     * @param duplicateId
     * @return key contains questionId,subject,flag,duplicateId
     */
    List<Map> findWithDuplicateByDuplicateId(@Param("duplicateId") Long duplicateId);

    List<Map<String, Long>> countGroupBySubject();

    int updateQuestionBizStatusBatch(@Param("questionIds") List<Long> questionIds,
                                     @Param("bizStatus") int bizStatus);


    List<Map<String, Long>> findIdBetweenAnd(@Param("startIndex") int startIndex,
                                @Param("endIndex")int endIndex);

    /**
     * 根据科目和底层知识点查询试题ID
     * @param subjectId
     * @param knowledgeIds
     * @return
     */
    List<Long> findIdBySubjectAndKnowledge(@Param("subjectId")Long subjectId,@Param("knowledgeIds") List<Long> knowledgeIds);

}
