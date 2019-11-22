package com.huatu.tiku.essay.repository.v2;

import com.huatu.tiku.essay.entity.correct.EssayPaperLabelTotal;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface EssayPaperLabelTotalRepository extends JpaRepository<EssayPaperLabelTotal, Long>, JpaSpecificationExecutor<EssayPaperLabelTotal> {

    EssayPaperLabelTotal findByAnswerIdAndStatusAndLabelFlag(long paperAnswerId, int status, int labelFlag);

    @Transactional
    @Modifying
    @Query("update EssayPaperLabelTotal em set em.status= -1  where em.id = ?1")
    int deletePaperLabel(long labelId);


    @Transactional
    @Modifying
    @Query("update EssayPaperLabelTotal t set t.labelFlag=?3  where t.answerId = ?1 and  t.status=?2")
    int updateLabelFlag(long answerId, int status, int labelFlag);


    List<EssayPaperLabelTotal> findByStatusAndBizStatusAndLabelFlag(Pageable pageable, int status, int bizStatus, int labelFlag);
}
