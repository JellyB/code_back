//package com.huatu.tiku.match.util;
//
//import com.alibaba.fastjson.JSONObject;
//import com.huatu.tiku.match.bo.MatchBo;
//import com.huatu.tiku.match.common.MatchConfig;
//import com.huatu.tiku.match.common.MatchInfoRedisKeys;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.dao.DataAccessException;
//import org.springframework.data.redis.connection.RedisConnection;
//import org.springframework.data.redis.core.RedisCallback;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.ValueOperations;
//import org.springframework.stereotype.Component;
//
//import java.util.concurrent.TimeUnit;
//
///**
// * 描述：
// *
// * @author biguodong
// * Create time 2018-10-18 上午10:34
// **/
//
//@Component
//@Slf4j
//public class RedisUtil {
//
//    @Autowired
//    private RedisTemplate<String, String> redisTemplate;
//
//    @Autowired
//    private MatchConfig matchConfig;
//
//
//    /**
//     * 检查缓存中模考大赛名称是否有更改，有的话，更新缓存
//     * @param matchBo
//     */
//    @Deprecated
//    public void checkMatchName(final MatchBo matchBo) {
//        if (matchBo.getEssayPaperId() <= 0) {
//            return;
//        }
//        final String cacheNameKey = MatchInfoRedisKeys.getPracticeInfoKey();
//        Object cacheName = redisTemplate.execute(new RedisCallback<Object>() {
//            @Override
//            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
//                return redisConnection.hGet(cacheNameKey.getBytes(), String.valueOf(matchBo.getMatchId()).getBytes());
//            }
//        });
//        if (cacheName == null || !cacheNameKey.equals(matchBo.getName())) {
//            log.info("hash redis writing ……,key={},value={}", matchBo.getMatchId(), matchBo.getName());
//            redisTemplate.execute(new RedisCallback() {
//                @Override
//                public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
//                    redisConnection.hSet(cacheNameKey.getBytes(), String.valueOf(matchBo.getMatchId()).getBytes(), JSONObject.toJSONString(matchBo.getName()).getBytes());
//                    return null;
//                }
//            });
//        }
//    }
//
//
//
//
//
//
//    /**
//     * 判断当前模考大赛是否结束
//     * 已经过了结束时间而且所有答题卡都已经处理
//     *
//     * @param paperId
//     * @return
//     */
//    public boolean isCurrentMatchFinish(int paperId, long endTime) {
//        String matchPracticeIdSetKey = MatchInfoRedisKeys.getMatchPracticeIdSetKey(paperId);
//        boolean isTimeEnd = System.currentTimeMillis() > endTime;
//        return isTimeEnd && redisTemplate.opsForSet().size(matchPracticeIdSetKey).intValue() == 0;
//    }
//
//
//    /**
//     * 判断申论考试是否结束且试卷已经全部处理
//     *
//     * @param matchBo
//     * @param status
//     * @return
//     */
//    public boolean isEssayMatchFinish(MatchBo matchBo, int status) {
//        /**
//         * 待处理的试卷set
//         */
//        String setKey = MatchInfoRedisKeys.getPublicUserSetPrefix(matchBo.getEssayPaperId());
//        boolean isTimeEnd = System.currentTimeMillis() > matchBo.getEssayEndTime() + TimeUnit.MINUTES.toMillis(matchConfig.getEssayDelayReportTime());
//        boolean isFinished = (status == 3);
//        return isTimeEnd && redisTemplate.opsForSet().size(setKey).intValue() == 0 && isFinished;
//    }
//
//}
