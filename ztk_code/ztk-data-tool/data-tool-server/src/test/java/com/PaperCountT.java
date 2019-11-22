package com;

import com.huatu.ztk.backend.BaseTestW;
import com.huatu.ztk.backend.mysql.dao.PaperQuestionSqlDao;
import com.huatu.ztk.backend.mysql.dao.PaperSqlDao;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by huangqingpeng on 2018/9/8.
 */
public class PaperCountT extends BaseTestW {
    private static final Logger logger = LoggerFactory.getLogger(PaperCountT.class);
    public final static String vhuatu_name = "v_huatu_paper_name";
    public final static String vhuatu_count = "v_huatu_paper_count";

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    PaperSqlDao paperSqlDao;

    @Autowired
    PaperQuestionSqlDao paperQuestionSqlDao;

    @Test
    public void test(){
        List<Map> allName = paperSqlDao.findAllName();
        List<Map> allCount = paperQuestionSqlDao.groupByCount();
        Map<String, String> nameMap = allName.stream().collect(Collectors.toMap(i -> String.valueOf(i.get("id")), i -> String.valueOf(i.get("name"))));
        Map<String, String> countMap = allCount.stream().collect(Collectors.toMap(i -> String.valueOf(i.get("id")), i -> String.valueOf(i.get("total"))));
        redisTemplate.opsForHash().putAll(vhuatu_name,nameMap);
        redisTemplate.opsForHash().putAll(vhuatu_count,countMap);
    }
}
