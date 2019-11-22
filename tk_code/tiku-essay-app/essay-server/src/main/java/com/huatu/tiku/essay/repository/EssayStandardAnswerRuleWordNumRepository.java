package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.EssayStandardAnswerRuleWordNum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by huangqp on 2017\12\6 0006.
 */
public interface EssayStandardAnswerRuleWordNumRepository extends JpaRepository<EssayStandardAnswerRuleWordNum, Long> {
    List<EssayStandardAnswerRuleWordNum> findByQuestionDetailIdAndBizStatusAndStatus(long questionId, int bizStatus, int status);

    @Transactional
    @Modifying
    @Query("update EssayStandardAnswerRuleWordNum t set t.status= -1  where t.questionDetailId = ?1")
    int delByQuestionDetailId(long questionDetailId);
}
