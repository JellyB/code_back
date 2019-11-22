package com.huatu.tiku.match.service.v1.enroll;

import com.huatu.ztk.commons.exception.BizException;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2018-10-19 上午9:36
 **/
public interface EnrollService {

    /**
     * 模考大赛报名接口
     * @param matchId
     * @param userId
     * @param userName
     * @param positionId
     * @param schoolId
     * @param schoolName
     * @return
     * @throws BizException
     */
    Object enroll(int matchId, int userId, String userName, int positionId, Long schoolId, String schoolName) throws BizException;
}
