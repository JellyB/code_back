package com.huatu.tiku.position.biz.service;

import com.huatu.tiku.position.base.service.BaseService;
import com.huatu.tiku.position.biz.domain.Enroll;

/**登记
 * @author wangjian
 **/
public interface EnrollService extends BaseService<Enroll,Long> {
    /**
     * 加入意向报名
     * @param userId 用户id
     * @param positionId 职位id
     */
    void addEnroll(Long userId, Long positionId);

    /**
     * 是否已加入意向报名
     *
     * @param userId     用户id
     * @param positionId 职位id
     * @param status 状态
     */
    Boolean findByUserIdAndPositionIdAndStatus(Long userId, Long positionId, Byte status);

    Integer getEnrollCount(Long id);

    /**
     * 移除意向报名
     * @param userId 用户id
     * @param enrollId 报名id
     */
    void removeEnroll(Long userId, Long enrollId);
}
