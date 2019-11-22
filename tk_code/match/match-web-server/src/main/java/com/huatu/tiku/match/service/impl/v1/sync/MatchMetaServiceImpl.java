package com.huatu.tiku.match.service.impl.v1.sync;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.match.dao.document.MatchDao;
import com.huatu.tiku.match.dao.document.MatchUserMetaDao;
import com.huatu.tiku.match.listener.enums.RabbitMatchKeyEnum;
import com.huatu.tiku.match.service.v1.paper.AnswerCardDBService;
import com.huatu.tiku.match.service.v1.sync.MatchMetaService;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.Match;
import com.huatu.ztk.paper.bean.MatchUserMeta;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by huangqingpeng on 2018/12/26.
 */
@Service
@Slf4j
public class MatchMetaServiceImpl implements MatchMetaService {

    @Autowired
    MatchUserMetaDao matchUserMetaDao;

    @Autowired
    MatchSyncStatusServiceImpl matchSyncStatusService;

    @Autowired
    AnswerCardDBService answerCardDBService;

    @Autowired
    MatchDao matchDao;

    @Autowired
    RabbitTemplate rabbitTemplate;
    @Value("${spring.profiles}")
    public String env;
    /**
     * 报名信息分页查询页面大小
     */
    private final int userMetaQueryLimit = 1000;
    /**
     * 答题卡分页查询页面大小
     */
    private final int AnswerCardQueryLimit = 100;

    /**
     * 全量查询报名人数
     *
     * @param matchId
     * @return
     */
    @Override
    public List<MatchUserMeta> findUserMetaByMatch(int matchId) {
        StopWatch stopWatch = new StopWatch("查询模考大赛报名人数：" + matchId);
        ArrayList<MatchUserMeta> userMetas = Lists.newArrayList();
        long index = 0;
        stopWatch.start();
        while (true) {
            List<MatchUserMeta> results = matchUserMetaDao.findByMatchId(matchId, index, userMetaQueryLimit);
            if (CollectionUtils.isEmpty(results)) {
                break;
            }
            userMetas.addAll(results);
            index = results.stream().map(MatchUserMeta::getUserId).max(Long::compareTo).get();
        }
        stopWatch.stop();
        log.info(stopWatch.prettyPrint());
        return userMetas;
    }


    public List<AnswerCard> findAnswerCardByIds(List<Long> ids) {
        StopWatch stopWatch = new StopWatch("答题卡批量查询：" + ids.size());
        int index = 0;
        List<AnswerCard> answerCards = Lists.newArrayList();
        while (true) {
            stopWatch.start("下标："+index);
            if(index >= ids.size()){
                break;
            }
            int end = index + AnswerCardQueryLimit < ids.size() ? index + AnswerCardQueryLimit : ids.size();
            List<Long> answerCardIds = ids.subList(index,end);
            List<AnswerCard> results = answerCardDBService.findById(answerCardIds);
            answerCards.addAll(results);
            index = end;
            stopWatch.stop();
        }
        log.info(stopWatch.prettyPrint());
        return answerCards;
    }

    /**
     * 同步某个模考大赛数据到mysql
     * @param matchId
     * @param essayPaperId
     */
    public void syncMatchMetaInfo2DB(int matchId, long essayPaperId){
        List<MatchUserMeta> userMetas = findUserMetaByMatch(matchId);
        userMetas.forEach(i->{
            sendMsg(i.getUserId(),matchId);
            matchSyncStatusService.addSyncUserInfo(matchId,i.getUserId());
        });
    }

    /**
     * 发送同步消息队列
     * @param userId
     * @param matchId
     */
    private void sendMsg(long userId, int matchId) {
        HashMap<Object, Object> map = Maps.newHashMap();
        map.put("userId",userId);
        map.put("matchId",matchId);
        rabbitTemplate.convertAndSend("", RabbitMatchKeyEnum.getQueue(RabbitMatchKeyEnum.MatchUserMetaSync,env),map);
    }

    public void syncMatchInfo(){
        List<Match> all = matchDao.findAll();
        all.sort(Comparator.comparing(Match::getStartTime));
        StopWatch stopWatch = new StopWatch("总时间");
        for (Match match : all) {
            stopWatch.start(match.getName());
            log.info("同步模考名称："+ match.getName());
            long essayPaperId = -1;
            try {
                essayPaperId = match.getEssayPaperId();
            }catch (Exception e){
                e.printStackTrace();
            }
            syncMatchMetaInfo2DB(match.getPaperId(),essayPaperId);
            stopWatch.stop();
        }
        log.info(stopWatch.prettyPrint());
    }
}
