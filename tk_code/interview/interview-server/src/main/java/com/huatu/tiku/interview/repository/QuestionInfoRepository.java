package com.huatu.tiku.interview.repository;

import com.huatu.tiku.interview.entity.po.QuestionInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Created by x6 on 2018/4/11.
 */
public interface QuestionInfoRepository extends JpaRepository<QuestionInfo, Long>,JpaSpecificationExecutor<QuestionInfo> {
    List<QuestionInfo> findByPaperIdAndStatus(long paperId, int status);

    @Modifying
    @Query("update QuestionInfo set status = ?2 where paperId = ?1 AND status = 1")
    void deleteQuestionByPaperId(long paperId, int status);
}
