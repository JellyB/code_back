//package com.huatu.tiku.teacher.service.impl.match;
//
//import com.google.common.base.Joiner;
//import com.google.common.collect.Lists;
//import com.google.common.collect.Maps;
//import com.huatu.tiku.enums.BaseInfo;
//import com.huatu.tiku.match.bean.entity.MatchEssayUserMeta;
//import com.huatu.tiku.service.impl.BaseServiceImpl;
//import com.huatu.tiku.teacher.service.common.AreaService;
//import com.huatu.tiku.teacher.service.match.MatchEssayUserMetaService;
//import com.huatu.ztk.paper.common.RedisKeyConstant;
//import org.apache.commons.collections.CollectionUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.connection.RedisConnection;
//import org.springframework.data.redis.connection.RedisZSetCommands;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.stream.Collectors;
//
///**
// * Created by huangqingpeng on 2018/10/17.
// */
//@Service
//public class MatchEssayUserMetaServiceImpl extends BaseServiceImpl<MatchEssayUserMeta> implements MatchEssayUserMetaService {
//    public MatchEssayUserMetaServiceImpl() {
//        super(MatchEssayUserMeta.class);
//    }
//
//    @Autowired
//    private RedisTemplate redisTemplate;
//
//    @Autowired
//    private AreaService areaService;
//
//    /* 申论模考的成绩Zset 无地区*/
//    public static String ESSAY_USER_SCORE_PREFIX = "e_u_s_p_1228";
//
//    @Override
//    public int persistenceByPaperId(long essayPaperId) {
//        int size = 0;
//        String enrollKey = RedisKeyConstant.getMockUserAreaPrefix(essayPaperId);
//        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
//        String essayUserScoreKey = getEssayUserScoreKey(essayPaperId);
//        try {
//            Map<byte[], byte[]> map = connection.hGetAll(enrollKey.getBytes());
//            if (null == map || map.size() == 0) {
//                return size;
//            }
//            //地区ID与名称映射关系
//            Map<Long, String> areaMap = areaService.areaList().stream().collect(Collectors.toMap(i -> i.getId(), i -> i.getName()));
//            //用户ID与报名地区ID映射关系
//            Map<Integer, Integer> positionMap = map.entrySet().stream().collect(Collectors.toMap(entry -> Integer.parseInt(new String(entry.getKey())), entry -> Integer.parseInt(new String(entry.getValue()))));
//            Set<RedisZSetCommands.Tuple> tuples = connection.zRangeWithScores(essayUserScoreKey.getBytes(), 0, -1);
//            Map<Long,Double> scoreMap = Maps.newHashMap();
//            if (CollectionUtils.isNotEmpty(tuples)) {
//                scoreMap.putAll(tuples.stream().collect(Collectors.toMap(i -> Long.parseLong(new String(i.getValue())), i -> i.getScore())));
//            }
//            List<MatchEssayUserMeta>  all = Lists.newArrayList();
//            for (Map.Entry<Integer, Integer> entry : positionMap.entrySet()) {
//                MatchEssayUserMeta metas = new  MatchEssayUserMeta();
//                metas.setEssayPaperId(essayPaperId);
//                metas.setUserId(entry.getKey());
//                Integer positionId = entry.getValue();
//                metas.setPositionId(positionId);
//                metas.setPositionName(areaMap.getOrDefault(positionId,"未知区域"));
//                Double score = scoreMap.get(entry.getKey());
//                if(null != score || score > 0){
//                    metas.setScore(score);
//                    metas.setIsAnswer(BaseInfo.YESANDNO.YES.getCode());
//                }else{
//                    metas.setScore(0D);
//                    metas.setIsAnswer(BaseInfo.YESANDNO.NO.getCode());
//                }
//                all.add(metas);
//                size ++ ;
//            }
//            insertAll(all);
//        } finally {
//            connection.close();
//        }
//        return size;
//    }
//
//    /**
//     * 用户申论模考成绩zSet
//     *
//     * @return
//     */
//    public static String getEssayUserScoreKey(long paperId) {
//        return "essay-server." + Joiner.on("_").join(ESSAY_USER_SCORE_PREFIX, paperId);
//    }
//}
