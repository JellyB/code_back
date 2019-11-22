package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.EssayLabelDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by x6 on 2018/7/6.
 */
public interface EssayLabelDetailRepository extends JpaRepository<EssayLabelDetail, Long> , JpaSpecificationExecutor<EssayLabelDetail> {


    /**
     * 根据totalId和状态查询相关的详细批注
     * @param totalId
     * @param status
     * @return
     */
    List<EssayLabelDetail> findByTotalIdAndStatus(long totalId, int status);


    @Transactional
    @Modifying
    @Query("update EssayLabelDetail d set d.status= -1 where d.totalId = ?1 ")
    void updateToDel(long totalId);



}
