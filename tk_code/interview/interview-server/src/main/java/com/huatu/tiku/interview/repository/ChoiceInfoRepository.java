package com.huatu.tiku.interview.repository;

import com.huatu.tiku.interview.entity.po.ChoiceInfo;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Created by x6 on 2018/4/11.
 */
public interface ChoiceInfoRepository extends JpaRepository<ChoiceInfo, Long> {
    List<ChoiceInfo> findByQuestionIdInAndStatus(List<Long> questionIdList, int status);

    @Modifying
    @Query("update ChoiceInfo set status = ?2 where questionId = ?1 AND status = 1")
    void deleteChoiceByQuestionId(long questionId, int status);
}
