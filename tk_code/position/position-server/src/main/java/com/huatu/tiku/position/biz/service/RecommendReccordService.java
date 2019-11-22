package com.huatu.tiku.position.biz.service;

import com.huatu.tiku.position.base.service.BaseService;
import com.huatu.tiku.position.biz.domain.RecommendReccord;

/**
 * @author wangjian
 **/
public interface RecommendReccordService extends BaseService<RecommendReccord,Long> {

    void saveX(Long id);

    Integer checkUpdate(Long id);
}
