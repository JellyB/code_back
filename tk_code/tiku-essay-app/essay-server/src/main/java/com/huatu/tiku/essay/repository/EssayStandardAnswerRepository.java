package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.EssayStandardAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by x6 on 2018/5/3.
 */
public interface EssayStandardAnswerRepository   extends JpaRepository<EssayStandardAnswer, Long> {

    @Transactional
    @Modifying
    @Query("update EssayStandardAnswer sa set sa.status= -1  where sa.id = ?1")
    int updateToDel(long id);

    List<EssayStandardAnswer> findByQuestionIdAndStatusOrderByIdAsc(long questionId, int status);

    @Transactional
    @Modifying
    @Query("update EssayStandardAnswer sa set sa.status= -1  where sa.questionId = ?1")
    int delByQuestionDetailId(long detailId);
}
