package com.huatu.tiku.match.service.v1.search;

import com.huatu.tiku.match.bo.MatchHeadUserBo;
import com.huatu.tiku.match.common.MatchSimpleStatus;
import com.huatu.ztk.commons.exception.BizException;

import java.util.List;

/**
 * @author huangqingpeng
 *
 */
public interface MatchStatusService {

    /**
     * 用户数据查询
     * @param userId
     * @param subject
     * @return
     */
    List<MatchHeadUserBo> getMatchHeadUserInfo(int userId, int subject) throws BizException;

}
