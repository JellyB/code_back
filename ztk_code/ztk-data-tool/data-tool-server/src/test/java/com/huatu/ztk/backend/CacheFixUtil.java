package com.huatu.ztk.backend;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 此工具用以处理cache 中个人收藏信息异常的情况
 * Created by junli on 2018/4/16.
 */
public class CacheFixUtil extends BaseTestW {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final int TTL = 360 * 3;

    @Test
    public void BaseTest() {
        //获取所有的知识点信息
        final List<String> list = getPointIdList();

        SetOperations<String, String> setOperations = redisTemplate.opsForSet();
        ZSetOperations zSet = redisTemplate.opsForZSet();
        final HashOperations hash = redisTemplate.opsForHash();
        System.out.println("");
        System.out.println("_____________________________");

        ExecutorService executorService = Executors.newFixedThreadPool(5);
        for (int index = 0; index < 5; index++) {
            executorService.execute(() -> {
                        String name = Thread.currentThread().getName();
                        int count = 0;
                        int changeCount = 0;
                        while (true) {
                            final String key = setOperations.pop("question:user:cache:collect");
                            if (StringUtils.isBlank(key)) {
                                break;
                            }
                            System.out.println(name + ",count = " + count++);
                            Boolean expire = redisTemplate.expire(key, TTL, TimeUnit.DAYS);
                            if (!expire) {
                                long l = System.currentTimeMillis();
                                list.parallelStream().forEach(pointId -> {
                                    String uid = getUidFromCollectHash(key);
                                    if (StringUtils.isNotBlank(uid)) {
                                        Long size = zSet.size(getCollectKey(uid, pointId));
                                        if (size != 0) {
                                            hash.put(key, pointId, size + "");
                                        }
                                    }
                                });
                                System.out.println(name + ",changeCount = " + changeCount++);
                                System.out.println(name + ",key :" + key + ", useTime = " + (System.currentTimeMillis() - l));
                            }
                        }
                    }
            );
        }

        try {
            while (true) {
                Thread.sleep(100 * 60 * 5);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取所有的知识点
     */
    private List<String> getPointIdList() {
        String sql = "SELECT pukey FROM v_knowledge_point WHERE bb102 = 1 " +
                "AND bl_sub IN ( SELECT id FROM v_new_subject WHERE catgory IN (1,3,200100045,200100047,200100000,200100002));";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        List<String> pukeyList = list.stream().map(map -> map.get("pukey").toString()).collect(Collectors.toList());
        return pukeyList;
        //return Arrays.asList("765","436","769","782");
    }

    /**
     * hash id 中获取 uid collect_count_$uid_1
     */
    private static String getUidFromCollectHash(String hash) {
        return hash.split("collect_count_")[1].split("_")[0];
    }

    /**
     * 获取收藏的节点信息
     */
    private static String getCollectKey(String uid, String point) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("collect_").append(uid)
                .append("_1")
                .append("_").append(point);
        return stringBuilder.toString();
    }
}
