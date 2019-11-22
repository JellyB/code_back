package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.EssayQuestionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by x6 on 2017/11/24.
 */
public interface EssayQuestionTypeRepository extends JpaRepository<EssayQuestionType, Long> {
    List<EssayQuestionType> findByStatusAndBizStatus(int status,int bizStatus);

    List<EssayQuestionType> findByPidAndStatusAndBizStatus(long pid,int status,int bizStatus);
}
