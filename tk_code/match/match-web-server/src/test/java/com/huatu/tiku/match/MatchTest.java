package com.huatu.tiku.match;

import com.alibaba.druid.support.json.JSONUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huatu.common.test.BaseWebTest;
import com.huatu.tiku.match.bean.entity.MatchUserMeta;
import com.huatu.tiku.match.common.MatchInfoRedisKeys;
import com.huatu.tiku.match.common.PaperErrorInfo;
import com.huatu.tiku.match.dao.document.AnswerCardDao;
import com.huatu.tiku.match.listener.impl.AnswerCardSubmitAsyncListener;
import com.huatu.tiku.match.service.MatchTestService;
import com.huatu.tiku.match.service.v1.meta.MatchUserMetaService;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by huangqingpeng on 2019/3/1.
 */
public class MatchTest extends BaseWebTest {

    @Autowired
    MatchUserMetaService matchUserMetaService;
    @Autowired
    AnswerCardDao answerCardDao;
    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    AnswerCardSubmitAsyncListener answerCardSubmitAsyncListener;

    @Test
    public void test() {
        Example example = new Example(MatchUserMeta.class);
        example.and().andEqualTo("matchId", 4001693).andNotEqualTo("practiceId", -1);
        List<MatchUserMeta> matchUserMetas = matchUserMetaService.selectByExample(example);
        List<Long> practiceIds = matchUserMetas.stream().map(MatchUserMeta::getPracticeId).collect(Collectors.toList());

        DBObject query1 = new BasicDBObject(); //setup the query criteria 设置查询条件
        query1.put("_id", new BasicDBObject("$in", practiceIds));
        DBObject fields = new BasicDBObject(); //only get the needed fields. 设置需要获取哪些域
        fields.put("_id", 1);
        fields.put("terminal", 1);
        DBCursor dbCursor = mongoTemplate.getCollection("ztk_answer_card").find(query1, fields);
        List<Integer> ids = Lists.newArrayList();
        while (dbCursor.hasNext()) {
            DBObject object = dbCursor.next();
            if (StringUtils.isNumeric(object.get("terminal").toString())) {
                ids.add(Integer.parseInt(object.get("terminal").toString()));
            }
        }
        Map<Integer, List<Integer>> map = IntStream.range(0, ids.size()).boxed().collect(Collectors.groupingBy(i -> ids.get(i)));
        Map<Integer, Integer> collect = map.entrySet().stream().collect(Collectors.toMap(i -> i.getKey(), i -> i.getValue().size()));
        System.out.println("collect = " + collect);
    }

    @Test
    public void test2() {
        int matchId = 4001836;
        Example example = new Example(MatchUserMeta.class);
        example.and().andEqualTo("matchId", matchId);
        List<MatchUserMeta> matchUserMetas = matchUserMetaService.selectByExample(example);
        for (MatchUserMeta matchUserMeta : matchUserMetas) {
            String positionEnrollHashKey = MatchInfoRedisKeys.getMatchPositionEnrollHashKey(matchId);
            HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
            hashOperations.put(positionEnrollHashKey, matchUserMeta.getUserId() + "", matchUserMeta.getPositionId() + "");
            redisTemplate.expire(positionEnrollHashKey, 15, TimeUnit.DAYS);

        }
    }

    /**
     * 清空报名缓存
     */
    @Test
    public void test23(){
        int matchId = 4001836;
        Example example = new Example(MatchUserMeta.class);
        example.and().andEqualTo("matchId", matchId).andNotEqualTo("practiceId", -1);
        List<MatchUserMeta> matchUserMetas = matchUserMetaService.selectByExample(example);
        if(CollectionUtils.isEmpty(matchUserMetas)){
            return;
        }
        for (MatchUserMeta matchUserMeta : matchUserMetas) {
            String userEnrollHashKey = MatchInfoRedisKeys.getUserEnrollHashKey(matchUserMeta.getMatchId(),matchUserMeta.getUserId());
            System.out.println("userEnrollHashKey = " + userEnrollHashKey);
            redisTemplate.delete(userEnrollHashKey);
        }
    }

    @Test
    public void test3() throws BizException {
        int positionTotal = matchUserMetaService.getPositionTotal(4001836, 802);
        System.out.println("positionTotal = " + positionTotal);
        MatchUserMeta report = matchUserMetaService.getReport(4001836, 233961179);
        System.out.println("JSONUtils.toJSONString(report) = " + JsonUtil.toJson(report));
    }

    @Test
    public void test4() {
        int matchId = 4001836;
        Example example = new Example(MatchUserMeta.class);
        example.and().andEqualTo("matchId", matchId);
        List<MatchUserMeta> matchUserMetas = matchUserMetaService.selectByExample(example);
        for (MatchUserMeta matchUserMeta : matchUserMetas) {
            String userEnrollKey = MatchInfoRedisKeys.getUserEnrollHashKey(matchId, matchUserMeta.getUserId());
            redisTemplate.delete(userEnrollKey);
        }
    }

    @Test
    public void test1(){
        HashMap<Object, Object> map = Maps.newHashMap();
        map.put("userId", 233982952);
        map.put("practiceId", 268637602380056819L);
        map.put("answerList", "[{\"answer\":\"1\",\"doubt\":0,\"questionId\":30006579},{\"answer\":\"24\",\"doubt\":0,\"questionId\":30006577},{\"answer\":\"1\",\"doubt\":0,\"questionId\":30006575},{\"answer\":\"3\",\"doubt\":0,\"questionId\":30006570},{\"answer\":\"2\",\"doubt\":0,\"questionId\":30006567},{\"answer\":\"3\",\"doubt\":0,\"questionId\":30006568},{\"answer\":\"2\",\"doubt\":0,\"questionId\":30006459},{\"answer\":\"3\",\"doubt\":0,\"questionId\":30006305},{\"answer\":\"2\",\"doubt\":0,\"questionId\":30006306},{\"answer\":\"1\",\"doubt\":0,\"questionId\":30006307},{\"answer\":\"3\",\"doubt\":0,\"questionId\":30006309},{\"answer\":\"3\",\"doubt\":0,\"questionId\":30006565},{\"answer\":\"2\",\"doubt\":0,\"questionId\":30006323},{\"answer\":\"0\",\"doubt\":0,\"questionId\":30006476},{\"answer\":\"0\",\"doubt\":0,\"questionId\":30006311},{\"answer\":\"0\",\"doubt\":0,\"questionId\":39409},{\"answer\":\"0\",\"doubt\":0,\"questionId\":59392},{\"answer\":\"0\",\"doubt\":0,\"questionId\":59394},{\"answer\":\"0\",\"doubt\":0,\"questionId\":30932},{\"answer\":\"0\",\"doubt\":0,\"questionId\":30933},{\"answer\":\"0\",\"doubt\":0,\"questionId\":58548},{\"answer\":\"0\",\"doubt\":0,\"questionId\":53955},{\"answer\":\"0\",\"doubt\":0,\"questionId\":53954},{\"answer\":\"0\",\"doubt\":0,\"questionId\":30934},{\"answer\":\"0\",\"doubt\":0,\"questionId\":30855},{\"answer\":\"0\",\"doubt\":0,\"questionId\":53953},{\"answer\":\"0\",\"doubt\":0,\"questionId\":51142},{\"answer\":\"0\",\"doubt\":0,\"questionId\":51136},{\"answer\":\"0\",\"doubt\":0,\"questionId\":51135},{\"answer\":\"0\",\"doubt\":0,\"questionId\":51134},{\"answer\":\"0\",\"doubt\":0,\"questionId\":51131},{\"answer\":\"0\",\"doubt\":0,\"questionId\":50979},{\"answer\":\"0\",\"doubt\":0,\"questionId\":50863},{\"answer\":\"0\",\"doubt\":0,\"questionId\":50862},{\"answer\":\"0\",\"doubt\":0,\"questionId\":30900},{\"answer\":\"0\",\"doubt\":0,\"questionId\":69809},{\"answer\":\"0\",\"doubt\":0,\"questionId\":69810},{\"answer\":\"0\",\"doubt\":0,\"questionId\":69811},{\"answer\":\"0\",\"doubt\":0,\"questionId\":69812},{\"answer\":\"0\",\"doubt\":0,\"questionId\":69813},{\"answer\":\"0\",\"doubt\":0,\"questionId\":69838},{\"answer\":\"0\",\"doubt\":0,\"questionId\":69834},{\"answer\":\"0\",\"doubt\":0,\"questionId\":69835},{\"answer\":\"0\",\"doubt\":0,\"questionId\":69836},{\"answer\":\"0\",\"doubt\":0,\"questionId\":69837}]");
        answerCardSubmitAsyncListener.onMessage(map);
    }
}
