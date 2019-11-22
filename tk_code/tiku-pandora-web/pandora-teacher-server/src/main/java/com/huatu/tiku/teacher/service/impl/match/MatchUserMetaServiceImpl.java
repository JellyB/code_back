package com.huatu.tiku.teacher.service.impl.match;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.entity.question.BaseQuestion;
import com.huatu.tiku.enums.BaseInfo;
import com.huatu.tiku.match.bean.entity.MatchUserMeta;
import com.huatu.tiku.match.enums.MatchInfoEnum;
import com.huatu.tiku.service.impl.BaseServiceImpl;
import com.huatu.tiku.teacher.dao.mongo.MatchDao;
import com.huatu.tiku.teacher.dao.mongo.MatchUserMetaDao;
import com.huatu.tiku.teacher.dao.question.MatchUserMetaMapper;
import com.huatu.tiku.teacher.service.match.MatchUserMetaService;
import com.huatu.ztk.paper.bean.Match;
import com.huatu.ztk.paper.common.AnswerCardStatus;
import com.huatu.ztk.paper.common.PaperRedisKeys;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by huangqingpeng on 2018/10/16.
 */
@Service
public class MatchUserMetaServiceImpl extends BaseServiceImpl<MatchUserMeta> implements MatchUserMetaService {
    public MatchUserMetaServiceImpl() {
        super(MatchUserMeta.class);
    }

    @Autowired
    private MatchUserMetaDao matchUserMetaDao;

    @Autowired
    private MatchDao matchDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MatchUserMetaMapper matchUserMetaMapper;

    @Override
    public int persistenceByPaperId(int matchId) {
        Match match = matchDao.findById(matchId);
        int size = 0;
        if (null == match) {
            return size;
        }
        List<MatchUserMeta> matchUserMetas = matchUserMetaDao.findByPaperId(matchId);
        Map<Long, Double> scoreMap = getScoreByCache(matchId);
        List<Long> practiceIds = matchUserMetas.stream().map(MatchUserMeta::getPracticeId).collect(Collectors.toList());
        Map<Long, Long> timeMap = findCardTime(practiceIds);
        long essayPaperId = match.getEssayPaperId();
        for (MatchUserMeta matchUserMeta : matchUserMetas) {
            Long practiceId = matchUserMeta.getPracticeId();
            Long time = timeMap.get(practiceId);
            Double score = scoreMap.get(practiceId);
            if (null == score) {
                matchUserMeta.setIsAnswer(MatchInfoEnum.AnswerStatus.NO_SUBMIT.getKey());
                matchUserMeta.setCardCreateTime(new Timestamp(time));
            } else {
                matchUserMeta.setIsAnswer(MatchInfoEnum.AnswerStatus.SUBMIT.getKey());
                matchUserMeta.setSubmitTime(new Timestamp(time));
            }
        }
        size = insertAll(matchUserMetas);
        return size;
    }

    private Map<Long, Long> findCardTime(List<Long> practiceIds) {
        DBObject queryObject = new BasicDBObject();
        queryObject.put("_id", new BasicDBObject("$in", practiceIds));
        DBObject fieldsObject = new BasicDBObject();
        fieldsObject.put("_id", 1);
        fieldsObject.put("createTime", 1);
        DBCursor dbCursor = mongoTemplate.getCollection("ztk_answer_card").find(queryObject, fieldsObject);
        Map<Long, Long> mapData = Maps.newHashMap();
        while (dbCursor.hasNext()) {
            DBObject object = dbCursor.next();
            long id = Long.parseLong(object.get("_id").toString());
            long time = Long.parseLong(object.get("createTime").toString());
            mapData.put(id, time);
        }
        return mapData;
    }

    private Map<Long, Double> getScoreByCache(int matchId) {
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        String practiceIdSore = PaperRedisKeys.getPaperPracticeIdSore(matchId);
        try {
            Set<RedisZSetCommands.Tuple> tuples = connection.zRangeWithScores(practiceIdSore.getBytes(), 0, -1);
            Map<Long, Double> scoreMap = tuples.stream().collect(Collectors.toMap(i -> Long.parseLong(new String(i.getValue())), i -> i.getScore()));
            return scoreMap;
        } finally {
            connection.close();
        }
    }


    /**
     * 新模考大赛
     * 根据模考大赛ID 从match_user_meta 表中查询模考信息
     *
     * @return
     */
    public List<MatchUserMeta> findByMatchId(int matchId) {
        tk.mybatis.mapper.entity.Example example = new tk.mybatis.mapper.entity.Example(MatchUserMeta.class);
        example.and().andEqualTo("matchId", matchId).andEqualTo("status",1);
        List<MatchUserMeta> matchUserMetas = selectByExample(example);
        return matchUserMetas;
    }

    @Override
    public List<MatchUserMeta> findOrderByScore(int paperId, int size) {
        List<Map> orderByScore = matchUserMetaMapper.findOrderByScore(paperId, size);
        if (CollectionUtils.isEmpty(orderByScore)) {
            return Lists.newArrayList();
        }
        return orderByScore.stream().map(i ->
                MatchUserMeta.builder().practiceId(MapUtils.getLong(i,"practice_id"))
                .matchId(MapUtils.getInteger(i,"match_id"))
                .score(MapUtils.getDouble(i,"score",0D))
                .positionId(MapUtils.getInteger(i,"position_id"))
                .positionName(MapUtils.getString(i,"position_name"))
                .build()
        ).collect(Collectors.toList());
    }

}
