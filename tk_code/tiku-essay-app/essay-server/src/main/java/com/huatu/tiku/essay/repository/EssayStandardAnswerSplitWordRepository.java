package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.EssayStandardAnswerSplitWord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by huangqp on 2017\12\6 0006.
 */
public interface EssayStandardAnswerSplitWordRepository extends JpaRepository<EssayStandardAnswerSplitWord, Long> {
    List<EssayStandardAnswerSplitWord> findByRelationIdInAndBizStatusAndStatus(List<Long> relations, int bizStatus, int status);
    List<EssayStandardAnswerSplitWord> findByRelationIdAndBizStatusAndStatus(Long relationId, int bizStatus, int status);
}
