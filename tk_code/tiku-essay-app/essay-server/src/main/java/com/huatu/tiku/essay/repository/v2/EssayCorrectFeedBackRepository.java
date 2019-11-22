package com.huatu.tiku.essay.repository.v2;

import com.huatu.tiku.essay.entity.correct.CorrectFeedBack;
import com.huatu.tiku.essay.essayEnum.EssayStatusEnum;
import com.huatu.tiku.essay.vo.admin.FeedBackStatisticVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EssayCorrectFeedBackRepository extends JpaRepository<CorrectFeedBack, Long>, JpaSpecificationExecutor<CorrectFeedBack> {

    List<CorrectFeedBack> findByAnswerIdAndStatus(long answerId, int status);

    List<CorrectFeedBack> findByAnswerIdIn(List<Long> answerId);

    List<CorrectFeedBack> findByStatusAndOrderIdIn(int status, List<Long> orderIds);

    List<CorrectFeedBack> findByUserIdAndStatus(long userId,int status);

    @Query("select new com.huatu.tiku.essay.vo.admin.FeedBackStatisticVO(sum(star), count(id)) from CorrectFeedBack where teacherId = ?1")
    FeedBackStatisticVO statisticTeacherScore(long teacherId);


}
