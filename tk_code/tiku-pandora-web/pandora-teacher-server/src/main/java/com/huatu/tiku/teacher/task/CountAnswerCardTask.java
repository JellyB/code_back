package com.huatu.tiku.teacher.task;

import com.google.common.collect.Maps;
import com.mongodb.BasicDBObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CountAnswerCardTask extends TaskService {


    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;


    private static String NANO_TIME_LOCK = "";
    private static final long CacheLockExpireTime = 60 * 24;      //分布式锁生命周期（分钟）--半小时
    /**
     * 分布式锁key（redis实现）
     */
    private static final String INIT_QUESTION_POINT_TREE_LOCK_KEY = "init_question_point_tree_lock_key";
    private static final String index_key = "count_question_key";
    private static final String size_key = "limit_size_key";
    private static final String map_key = "count_map_key";
    private static Map<Integer, Long> map = Maps.newHashMap();
    private static long start = 0L;
    private static long end = Long.MAX_VALUE;
    private static int size = 1_000;
    private static long date = 1546272000000L;

//    @Scheduled(fixedRate = 60000 * 60 * 24)
    public void initQuestionPointTree() {
        task();
    }

    /**
     * 定时任务实现逻辑
     */
    @Override
    public void run() {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(map_key);
        if (MapUtils.isNotEmpty(entries)) {
            map.putAll(entries.entrySet().stream().collect(Collectors.toMap(i -> Integer.parseInt(String.valueOf(i.getKey())), i -> Long.parseLong(String.valueOf(i.getValue())))));
        }
        setStart(getStart());
        while (true) {
            StopWatch stopWatch = new StopWatch("answerCardQuery");
            try {
                BasicDBObject query1 = new BasicDBObject(); //setup the query criteria 设置查询条件
                stopWatch.start(start + "");
                query1.put("_id", new BasicDBObject("$gt", start));
                BasicDBObject fields = new BasicDBObject(); //only get the needed fields. 设置需要获取哪些域
                fields.put("_id", 1);
                fields.put("createTime", 1);
                fields.put("type", 1);
                fields.put("answers", 1);
                BasicQuery basicQuery = new BasicQuery(query1.toJson(), fields.toJson());
                basicQuery.limit(size);
                List<HashMap> list = mongoTemplate.find(basicQuery, HashMap.class, "ZtkAnswerCard");
                stopWatch.stop();
                if (CollectionUtils.isEmpty(list)) {
                    break;
                }
                Map<Integer, Long> collect = list.stream().filter(i -> MapUtils.getLong(i, "createTime") > date).collect(Collectors.groupingBy(i -> MapUtils.getInteger(i, "type"), Collectors.summingLong(i -> {
                    List answers = (List) i.get("answers");
                    return answers.size();
                })));
                OptionalLong id1 = list.parallelStream().mapToLong(i -> MapUtils.getLong(i, "_id")).max();
                if (id1.isPresent()) {
                    setStart(id1.getAsLong());
                    mergeMap(map, collect);
                    redisTemplate.opsForHash().putAll(map_key, map.entrySet().stream().collect(Collectors.toMap(i -> i.getKey().toString(), i -> i.getValue().toString())));
                } else {
                    break;
                }
                if (start >= end) {
                    break;
                }
            } finally {
                System.out.println("stopWatch.prettyPrint() = " + stopWatch.prettyPrint());
            }
        }
        System.out.println("map = " + map);
    }

    public long getStart() {
        String s = redisTemplate.opsForValue().get(index_key);
        if (null != s) {
            return Long.parseLong(s);
        }
        return 0;
    }

    public void setStart(long start) {
        this.start = start;
        System.out.println("start = " + start);
        redisTemplate.opsForValue().set(index_key, start + "");
    }

    private Map<Integer, Long> mergeMap(Map<Integer, Long> map, Map<Integer, Long> collect) {

        for (Map.Entry<Integer, Long> entry : collect.entrySet()) {
            Long value = entry.getValue();
            Long orDefault = map.getOrDefault(entry.getKey(), 0L);
            map.put(entry.getKey(), value + orDefault);
        }
        return map;
    }

    @Override
    protected long getExpireTime() {
        return CacheLockExpireTime;
    }


    @Override
    public void unlock() {
        log.info("解锁---------CountAnswerCardTask");
        super.unlock();
    }

    @Override
    public String getCacheKey() {
        return INIT_QUESTION_POINT_TREE_LOCK_KEY;
    }

    @Override
    public String getNanoTime() {
        return NANO_TIME_LOCK;
    }

    @Override
    public void setNanoTime(String nanoTime) {
        NANO_TIME_LOCK = nanoTime;
    }


}
