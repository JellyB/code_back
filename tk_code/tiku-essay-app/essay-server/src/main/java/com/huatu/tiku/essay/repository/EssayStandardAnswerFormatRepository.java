package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.EssayStandardAnswerFormat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by huangqp on 2017\12\6 0006.
 */
public interface EssayStandardAnswerFormatRepository extends JpaRepository<EssayStandardAnswerFormat, Long> {
    List<EssayStandardAnswerFormat> findByQuestionDetailIdAndBizStatusAndStatus(long detailId, int bizStatus, int status);

    @Transactional
    @Modifying
    @Query("update EssayStandardAnswerFormat t set t.status= -1  where t.questionDetailId = ?1")
    int delByQuestionDetailId(long questionDetailId);


}
