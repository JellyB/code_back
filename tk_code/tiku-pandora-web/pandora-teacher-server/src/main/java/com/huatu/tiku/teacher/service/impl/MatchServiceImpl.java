package com.huatu.tiku.teacher.service.impl;

import com.huatu.tiku.teacher.dao.mongo.MatchDao;
import com.huatu.tiku.teacher.service.MatchService;
import com.huatu.ztk.paper.bean.Match;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by huangqp on 2018\7\7 0007.
 */
@Service
public class MatchServiceImpl implements MatchService {

    @Autowired
    MatchDao matchDao;
    /**
     * 查询模考大赛信息
     * @param matchId
     * @return
     */
    @Override
    public Match findById(int matchId) {
        return matchDao.findById(matchId);
    }
}

