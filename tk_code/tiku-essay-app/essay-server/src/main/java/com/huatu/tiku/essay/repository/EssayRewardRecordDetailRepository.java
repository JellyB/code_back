package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.EssayRewardRecordDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 赠送商品明细
 *
 * @author geek-s
 * @date 2019-07-31
 */
public interface EssayRewardRecordDetailRepository extends JpaRepository<EssayRewardRecordDetail, Long>, JpaSpecificationExecutor<EssayRewardRecordDetail> {


}
