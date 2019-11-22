package com.huatu.tiku.teacher.service.match;

import com.huatu.tiku.match.bean.entity.MatchUserMeta;
import com.huatu.tiku.service.BaseService;

import java.util.List;

/**
 * 模考大赛用户数据统计处理
 * Created by huangqingpeng on 2018/10/16.
 */
public interface MatchUserMetaService extends BaseService<MatchUserMeta> {

    /**
     * 同步用户模考大赛数据到mysql
     *
     * @param matchId
     * @return
     */
    int persistenceByPaperId(int matchId);


    /**
     * 根据模考大赛ID 从match_user_meta 表中查询模考信息
     *
     * @param matchId 模考大赛ID
     * @return
     */
    List<MatchUserMeta> findByMatchId(int matchId);


    List<MatchUserMeta> findOrderByScore(int paperId, int size);
}
