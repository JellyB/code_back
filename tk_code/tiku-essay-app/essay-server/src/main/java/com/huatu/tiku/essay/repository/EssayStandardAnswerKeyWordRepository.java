package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.EssayStandardAnswerKeyWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by huangqp on 2017\12\6 0006.
 */
public interface EssayStandardAnswerKeyWordRepository extends JpaRepository<EssayStandardAnswerKeyWord, Long> {
    List<EssayStandardAnswerKeyWord> findByQuestionDetailIdAndBizStatusAndStatusAndTypeInOrderByTypeAsc(long questionId, int bizStatus, int status, Integer[] types);

    List<EssayStandardAnswerKeyWord> findByQuestionDetailIdAndBizStatusAndStatusInOrderByIdAsc(long questionId, int bizStatus, int status);

    @Transactional
    @Modifying
    @Query("update EssayStandardAnswerKeyWord t set t.status= -1  where t.questionDetailId = ?1")
    int delByQuestionDetailId(long questionDetailId);

    @Transactional
    @Modifying
    @Query("update EssayStandardAnswerKeyWord t set t.status= -1  where t.questionDetailId = ?1 and type not in ?2")
    int delByQuestionDetailIdByTypesNotIn(long questionDetailId, List<Integer> list);
}
