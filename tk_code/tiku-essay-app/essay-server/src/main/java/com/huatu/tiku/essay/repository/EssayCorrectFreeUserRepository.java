package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.EssayCorrectFreeUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Created by x6 on 2018/2/6.
 */
public interface EssayCorrectFreeUserRepository extends JpaRepository<EssayCorrectFreeUser,Long> {
    @Query("select em from EssayCorrectFreeUser em where em.userId = ?1 and em.status = ?2 and em.bizStatus = ?3 and em.startTime <= current_timestamp" +
            " and em.endTime >= current_timestamp order by em.endTime desc")
    List<EssayCorrectFreeUser> findByUserIdAndStatusAndBizStatus(int userId,int status,int bizStatus);

    @Query("select em.userId from EssayCorrectFreeUser em where em.status = ?1 and em.bizStatus = ?2 and em.startTime <= current_timestamp" +
            " and em.endTime >= current_timestamp ")
    List<Integer>  findUserIdByStatusAndBizStatus(int status, int bizStatus);
}
