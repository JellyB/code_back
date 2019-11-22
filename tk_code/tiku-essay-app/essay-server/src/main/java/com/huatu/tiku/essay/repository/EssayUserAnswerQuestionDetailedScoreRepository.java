package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.EssayUserAnswerQuestionDetailedScore;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by x6 on 2017/12/5.
 */
public interface EssayUserAnswerQuestionDetailedScoreRepository extends JpaRepository<EssayUserAnswerQuestionDetailedScore, Long> {
    List<EssayUserAnswerQuestionDetailedScore> findByQuestionAnswerIdAndStatusOrderBySequenceNumberAsc(long answerId,int status);
    List<EssayUserAnswerQuestionDetailedScore> findByQuestionAnswerIdAndStatus(long answerId,int status, Sort sort);

    Integer countByQuestionAnswerIdAndTypeAndStatus(long id,int i,int status);
}
