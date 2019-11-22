package com.huatu.tiku.match.service.v1.sync;

import com.huatu.ztk.paper.bean.MatchUserMeta;

import java.util.List;

/**
 * 将模考大赛数据同步到mysql表中
 * Created by huangqingpeng on 2018/12/26.
 */
public interface MatchMetaService {

    List<MatchUserMeta> findUserMetaByMatch(int matchId);

    public void syncMatchInfo();

    public void syncMatchMetaInfo2DB(int matchId, long essayPaperId);
}
