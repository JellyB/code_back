package com.huatu.tiku.interview.repository;

import com.huatu.tiku.interview.entity.po.QuestionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by x6 on 2018/4/11.
 */
public interface QuestionAnswerRepository extends JpaRepository<QuestionAnswer, Long> {

    List<QuestionAnswer> findByOpenIdAndStatus(String openId, int status);

    List<QuestionAnswer> findByOpenIdAndQuestionIdInAndStatus(String openId, LinkedList<Long> questionIds, int status);

    List<QuestionAnswer> findByMockIdAndStatus(long id, int status);
    @Transactional
    @Modifying
    @Query("update QuestionAnswer qa set qa.status= -1  where qa.mockId = ?1")
    int updateToDeleteByMockId(long id);

    List<QuestionAnswer> findByOpenIdAndPushIdAndStatus(String openId, long pushId, int status);
}
