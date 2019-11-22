package com.huatu.tiku.match.service.impl.v1.sync;

import com.google.common.collect.Lists;
import com.huatu.tiku.match.common.MatchInfoRedisKeys;
import com.huatu.tiku.match.dao.document.MatchDao;
import com.huatu.tiku.match.service.v1.meta.MatchUserMetaService;
import com.huatu.ztk.paper.bean.Match;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 模考大赛试卷是否同步的状态维护
 * Created by huangqingpeng on 2019/2/27.
 */
@Slf4j
@Service
public class MatchSyncStatusServiceImpl {

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    MatchDao matchDao;

    @Autowired
    MatchUserMetaService matchUserMetaService;

    /**
     * 考试结束多久之后，试卷不能再次同步
     */
    public final static int deployTime = 30;
    /**
     * 提前多少分钟停止同步
     */
    public final static int preTime = 30;
    /**
     * 考试结束多久后开始同步
     */
    public final static int aftTime = 10;

    /**
     * 获得试卷的同步状态
     *
     * @return
     */
    public SyncStatus getSyncStatus(int matchId) {
        String matchSyncStatusHashKey = MatchInfoRedisKeys.getMatchSyncStatusHashKey();
        HashOperations hashOperations = redisTemplate.opsForHash();
        Object o = hashOperations.get(matchSyncStatusHashKey, matchId + "");
        int status = -1;
        try {
            if (o != null) {
                status = Integer.parseInt(String.valueOf(o));
            }
        } catch (Exception e) {
            log.error("getSyncStatus error,matchId = {} ,value = {}", matchId, String.valueOf(o));
            e.printStackTrace();
        }
        return SyncStatus.create(status);
    }

    /**
     * 保存同步状态
     */
    public void saveSyncStatus(int matchId, SyncStatus syncStatus) {
        String matchSyncStatusHashKey = MatchInfoRedisKeys.getMatchSyncStatusHashKey();
        HashOperations hashOperations = redisTemplate.opsForHash();
        hashOperations.put(matchSyncStatusHashKey, matchId + "", syncStatus.getCode() + "");
    }

    /**
     * 弹出一个待同步的试卷ID
     * @return -1表示没有待同步的ID或者有试卷正在同步过程中
     */
    public int popWaitingId(){
        List<String> runningIds = getMatchIdByStatus(SyncStatus.Running);
        if(CollectionUtils.isNotEmpty(runningIds)){
            return -1;
        }
        List<String> waitingIds = getMatchIdByStatus(SyncStatus.Waiting);
        if(CollectionUtils.isEmpty(waitingIds)){
            fillWaitingMatch();
        }
        waitingIds = getMatchIdByStatus(SyncStatus.Waiting);
        if(CollectionUtils.isEmpty(waitingIds)){
            return -1;
        }
        return Integer.parseInt(waitingIds.get(0));
    }
    /**
     * 根据状态查询模考大赛ID集合
     *
     * @param syncStatus
     * @return
     */
    public List<String> getMatchIdByStatus(SyncStatus syncStatus) {
        String matchSyncStatusHashKey = MatchInfoRedisKeys.getMatchSyncStatusHashKey();
        HashOperations<String,String,String> hashOperations = redisTemplate.opsForHash();
        Map<String, String> entries = hashOperations.entries(matchSyncStatusHashKey);
        switch (syncStatus) {
            case NoInit: {
                return findMatchNoInit(entries);
            }
            case Waiting:
            case NextWaiting:
            case Running:
            case Finished:
            default:{
                if(null == entries || entries.size() == 0){
                    return Lists.newArrayList();
                }
                return entries.entrySet().stream().filter(i -> i.getValue().equals(syncStatus.getCode() + "")).map(Map.Entry::getKey).collect(Collectors.toList());
            }
        }
    }

    private List<String> findMatchNoInit(Map<String,String> entries) {
        List<Match> all = matchDao.findAll();
        List<String> ids = all.stream().map(Match::getPaperId).map(String::valueOf).collect(Collectors.toList());
        if (null != entries && entries.size() > 0) {
            ids.removeIf(i->null != entries.get(i));
        }
        return ids;
    }

    public void addSyncUserInfo(int matchId,long userId){
        String syncUserIdSetKey = MatchInfoRedisKeys.getSyncUserIdSetKey(matchId);
        SetOperations<String,String> setOperations = redisTemplate.opsForSet();
        setOperations.add(syncUserIdSetKey,userId+"");
        redisTemplate.expire(syncUserIdSetKey,1,TimeUnit.HOURS);
    }

    public void removeSyncUserInfo(int matchId,long userId){
        String syncUserIdSetKey = MatchInfoRedisKeys.getSyncUserIdSetKey(matchId);
        SetOperations<String,String> setOperations = redisTemplate.opsForSet();
        setOperations.remove(syncUserIdSetKey,userId+"");
        synchronized (this){
            //某个模考大赛同步完成之后，如果考试还未结束，则修改状态为再次等待同步，否则，修改状态为完成同步
            Long size = setOperations.size(syncUserIdSetKey);
            Match match = matchDao.findById(matchId);
            long endTime = match.getEndTime() + TimeUnit.MINUTES.toMillis(deployTime);
            long currentTimeMillis = System.currentTimeMillis();
            if(size.intValue() == 0){
                boolean finished = matchUserMetaService.isFinished(matchId);
                if(currentTimeMillis > endTime && finished){
                    saveSyncStatus(matchId,SyncStatus.Finished);
                }else{
                    saveSyncStatus(matchId,SyncStatus.NextWaiting);
                }
                unlock();
            }

        }
    }

    private void unlock() {
        String syncMatchKey = MatchInfoRedisKeys.getSyncMatchKey();
        redisTemplate.delete(syncMatchKey);
    }

    /**
     * 补充需要等待同步的模考大赛
     */
    public void fillWaitingMatch(){
        List<String> waitingIds = getMatchIdByStatus(SyncStatus.Waiting);
        if(CollectionUtils.isNotEmpty(waitingIds)){
            return;
        }
        List<String> noInitIds = getMatchIdByStatus(SyncStatus.NoInit);
        if(CollectionUtils.isNotEmpty(noInitIds)){
            for (String noInitId : noInitIds) {
                saveSyncStatus(Integer.parseInt(noInitId),SyncStatus.Waiting);
            }
            return;
        }
        List<String> nextIds = getMatchIdByStatus(SyncStatus.NextWaiting);
        if(CollectionUtils.isNotEmpty(nextIds)){
            for (String nextId : nextIds) {
                saveSyncStatus(Integer.parseInt(nextId),SyncStatus.Waiting);
            }
            return;
        }
    }

    /**
     * 获取同步锁
     * @param matchId
     * @return
     */
    public boolean startSync(int matchId){
        String syncMatchKey = MatchInfoRedisKeys.getSyncMatchKey();
        ValueOperations<String,String> valueOperations = redisTemplate.opsForValue();
        Boolean startFlag = valueOperations.setIfAbsent(syncMatchKey, matchId + "");
        if(startFlag){
            saveSyncStatus(matchId,SyncStatus.Running);
        }
        return startFlag;
    }


    public void countSyncInfo(){
        log.info("统计同步现状");
        for (SyncStatus syncStatus : SyncStatus.values()) {
            log.info("状态：{}，模考大赛Id:{}",syncStatus.getName(),getMatchIdByStatus(syncStatus));
        }
    }

    /**
     * 检查RUNNING中的试卷是否已经同步完成，如果同步完成，直接修改状态
     */
    public void checkRunningId() {
        synchronized (this){
            List<String> matchIdByStatus = getMatchIdByStatus(SyncStatus.Running);
            if(CollectionUtils.isNotEmpty(matchIdByStatus)){
                for (String idByStatus : matchIdByStatus) {
                    removeSyncUserInfo(Integer.parseInt(idByStatus),-1);
                }
            }else{
                unlock();
            }
        }
    }

    public boolean checkRunningTime() {
        List<Match> currentMatchList = matchDao.findUsefulMatch();
        long currentTimeMillis = System.currentTimeMillis();
        for (Match match : currentMatchList) {
            long startTime = match.getStartTime() - TimeUnit.MINUTES.toMillis(preTime);
            long endTime = match.getEndTime() + TimeUnit.MINUTES.toMillis(aftTime);
            if(startTime < currentTimeMillis && endTime > currentTimeMillis){
                return false;
            }
        }
        return true;
    }

    @AllArgsConstructor
    @Getter
    public enum SyncStatus {
        NoInit(-1, "未初始化"),
        Waiting(0, "等待同步"),
        NextWaiting(1,"再次等待同步"),
        Running(2, "正在同步"),
        Finished(3, "完成同步"),
        ;

        private int code;
        private String name;

        public static SyncStatus create(int status) {
            for (SyncStatus syncStatus : SyncStatus.values()) {
                if (status == syncStatus.getCode()) {
                    return syncStatus;
                }
            }
            return SyncStatus.NoInit;
        }
    }
}
