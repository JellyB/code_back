package com.huatu.tiku.position.biz.respository;

import com.huatu.tiku.position.base.repository.BaseRepository;
import com.huatu.tiku.position.biz.domain.Enroll;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * @author wangjian
 **/
public interface EnrollRepository extends BaseRepository<Enroll,Long> {
    /**
     * 查询意向报名记录
     */
    Enroll findByUserIdAndPositionIdAndStatus(Long userId,Long positionId, Byte status);

    @Query(value = "select count(e) from Enroll e where e.positionId = ?1 and e.status = 1")
    Integer getEnrollCount(Long positionId);

    @Modifying
    @Query("update Enroll e set e.status = 0 where e.userId = ?1 and e.positionId = ?2")
    void removeEnroll(Long userId, Long positionId);
}
