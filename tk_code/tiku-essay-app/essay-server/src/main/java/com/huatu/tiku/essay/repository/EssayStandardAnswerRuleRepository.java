package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.EssayStandardAnswerRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by huangqp on 2017\12\6 0006.
 */
public interface EssayStandardAnswerRuleRepository extends JpaRepository<EssayStandardAnswerRule, Long> {
    List<EssayStandardAnswerRule> findByQuestionDetailIdAndBizStatusAndStatus(long questionId, int bizStatus, int status);
    List<EssayStandardAnswerRule> findByQuestionDetailIdInAndBizStatusAndStatus(List<Long> questionIds, int bizStatus, int status);
    List<EssayStandardAnswerRule> findByQuestionDetailIdAndTypeAndBizStatusAndStatus(long questionId,int type ,int bizStatus, int status);

    @Transactional
    @Modifying
    @Query("update EssayStandardAnswerRule t set t.status= -1  where t.questionDetailId = ?1")
    int delByQuestionDetailId(long questionDetailId);
}
