package com.huatu.tiku.teacher.service;

import com.huatu.ztk.paper.bean.Match;

/**
 * mongo - ztk_match
 * Created by huangqp on 2018\7\7 0007.
 */
public interface MatchService {

    /**
     * 查询模考大赛信息
     *
     * @param matchId
     * @return
     */
    Match findById(int matchId);
}
