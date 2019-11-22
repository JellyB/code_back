package com.huatu.tiku.position.biz.service;

import com.huatu.tiku.position.base.service.BaseService;
import com.huatu.tiku.position.biz.domain.BrowseRecord;

/**
 * @author wangjian
 **/
public interface BrowseRecordService  extends BaseService<BrowseRecord,Long> {
    void addRecord(Long userId, Long positionId);

    Integer addPositionRemark(Long userId, Long positionId, Boolean accordFlag);

    BrowseRecord findByUserIdAndPositionId(Long userId, Long positionId);

    Integer addPositionCollection(Long userId, Long positionId, Boolean flag);
}
