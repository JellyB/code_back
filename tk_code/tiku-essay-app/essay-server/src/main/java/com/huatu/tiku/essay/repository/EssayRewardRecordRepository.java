package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.EssayRewardRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 赠送批改次数
 */
public interface EssayRewardRecordRepository extends JpaRepository<EssayRewardRecord, Long> , JpaSpecificationExecutor<EssayRewardRecord> {


}
