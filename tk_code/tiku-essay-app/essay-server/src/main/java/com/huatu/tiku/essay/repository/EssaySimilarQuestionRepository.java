package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.EssaySimilarQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by x6 on 2017/11/30.
 */
public interface EssaySimilarQuestionRepository extends JpaRepository<EssaySimilarQuestion, Long> , JpaSpecificationExecutor<EssaySimilarQuestion> {
    @Transactional
    @Modifying
    @Query("update EssaySimilarQuestion qm set qm.status= -1  where qm.similarId = ?1")
    int upToDeleteBySimilarId(long similarId);
    List<EssaySimilarQuestion> findByQuestionBaseIdAndSimilarId(long questionBaseId, long similarId);

    List<EssaySimilarQuestion> findByQuestionBaseIdAndSimilarIdAndStatus(long questionBaseId, long similarId,int status);

    List<EssaySimilarQuestion> findBySimilarId(long similarId);

    @Query("select qb.questionBaseId from  EssaySimilarQuestion qb  where qb.similarId  = ?1 and qb.status = ?2 ")
    List<Long> findQuestionBaseIdBySimilarIdAndStatus(long similarId,int status);

    List<EssaySimilarQuestion> findBySimilarIdAndStatus(long similarId,int status);

    @Query("select qb.similarId from  EssaySimilarQuestion qb  where qb.questionBaseId  = ?1 and qb.status = ?2 ")
    List<Long> findGroupIdByQuestionBaseIdAndStatus(long questionBaseId,int status);

    @Transactional
    @Modifying
    @Query("update EssaySimilarQuestion qm set qm.status= -1  where  qm.questionBaseId in ?1")
    int upToDeleteByQuestionId(List<Long> questionBaseIdList);

    List<EssaySimilarQuestion> findByQuestionBaseIdAndStatus(long questionBaseId, int status);
}
