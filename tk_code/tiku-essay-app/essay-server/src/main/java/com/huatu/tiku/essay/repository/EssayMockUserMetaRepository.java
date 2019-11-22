package com.huatu.tiku.essay.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.huatu.tiku.essay.entity.EssayMockUserMeta;

/**
 * Created by x6 on 2018/7/3.
 */
public interface EssayMockUserMetaRepository  extends JpaRepository<EssayMockUserMeta, Long> {

    @Transactional
    @Modifying
    @Query("update EssayMockUserMeta em set em.status = -1 where em.paperId = ?1 and em.userId = ?2 ")
    int updateToDel(long paperId, int userId);

    List<EssayMockUserMeta> findByPaperIdAndStatus(long paperId, int status);
}
