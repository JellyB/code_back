package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.EssayStandardAnswerKeyPhrase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by huangqp on 2017\12\6 0006.
 */
public interface EssayStandardAnswerKeyPhraseRepository extends JpaRepository<EssayStandardAnswerKeyPhrase, Long> {
    List<EssayStandardAnswerKeyPhrase> findByQuestionDetailIdAndBizStatusAndStatus(long questionId, int bizStatus, int status);

    /**
     * 查询某种类型的关键句的主体信息
     * @param questionId
     * @param type
     * @param bizStatus
     * @param status
     * @return
     */
    List<EssayStandardAnswerKeyPhrase> findByQuestionDetailIdAndTypeAndBizStatusAndStatus(long questionId, int type, int bizStatus, int status);


    @Transactional
    @Modifying
    @Query("update EssayStandardAnswerKeyPhrase t set t.status= -1  where t.questionDetailId = ?1")
    int delByQuestionDetailId(long questionDetailId);

    @Transactional
    @Modifying
    @Query("update EssayStandardAnswerKeyPhrase t set t.status= -1  where t.questionDetailId = ?1 and type not in ?2")
    int delByQuestionDetailIdByTypesNotIn(long questionDetailId, List<Integer> list);

    List<EssayStandardAnswerKeyPhrase> findByPidAndTypeAndBizStatusAndStatus(long pid, int applicationKeyphraseType, int bizStatus, int status);

    List<EssayStandardAnswerKeyPhrase> findByQuestionDetailIdAndTypeAndBizStatusAndStatusAndPid(long questionId, int applicationKeyphraseType, int bizStatus, int status, long pid);

    List<EssayStandardAnswerKeyPhrase> findByQuestionDetailIdAndTypeAndBizStatusAndStatusAndPidIn(long questionId, int applicationKeyphraseType, int bizStatus, int status, List<Long> pids);

    List<EssayStandardAnswerKeyPhrase> findByQuestionDetailIdInAndStatus(long questionId, int status);


}
