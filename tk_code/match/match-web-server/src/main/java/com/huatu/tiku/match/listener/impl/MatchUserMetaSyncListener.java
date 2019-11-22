package com.huatu.tiku.match.listener.impl;

import com.huatu.tiku.match.dao.document.AnswerCardDao;
import com.huatu.tiku.match.dao.document.MatchDao;
import com.huatu.tiku.match.dao.document.MatchUserMetaDao;
import com.huatu.tiku.match.listener.ListenerService;
import com.huatu.tiku.match.service.impl.v1.sync.MatchSyncStatusServiceImpl;
import com.huatu.tiku.match.service.v1.meta.MatchUserMetaService;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.Match;
import com.huatu.ztk.paper.bean.MatchUserMeta;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by huangqingpeng on 2019/2/25.
 */
@Slf4j
@Component
public class MatchUserMetaSyncListener implements ListenerService {

    @Autowired
    MatchUserMetaService matchUserMetaService;
    @Autowired
    MatchUserMetaDao matchUserMetaDao;
    @Autowired
    AnswerCardDao answerCardDao;
    @Autowired
    MatchDao matchDao;
    @Autowired
    MatchSyncStatusServiceImpl matchSyncStatusService;
    @Override
    public void onMessage(Map message) {
        try{
            log.info("message start={}", message);
            Integer matchId = MapUtils.getInteger(message, "matchId");
            Long userId = MapUtils.getLong(message, "userId");
            Match match = matchDao.findById(matchId);
            long essayPaperId = match.getEssayPaperId();
            MatchUserMeta matchUserMeta = matchUserMetaDao.findOneByUserId(matchId, userId);
            long practiceId = matchUserMeta.getPracticeId();
            if(practiceId > -1){
                AnswerCard answerCard = answerCardDao.findById(practiceId);
                matchUserMetaService.sync2DB(matchUserMeta, answerCard, essayPaperId);
            } else {
                matchUserMetaService.sync2DB(matchUserMeta, null, essayPaperId);
            }
            //同步完成，删除缓存数据，等缓存数据清空后，证明该试卷同步完成，释放同步锁
            matchSyncStatusService.removeSyncUserInfo(matchId,userId);
        }catch (Exception e){
            log.error("MatchUserMetaSyncListener handler message error,message={}",message);
            e.printStackTrace();
        }

    }
}
