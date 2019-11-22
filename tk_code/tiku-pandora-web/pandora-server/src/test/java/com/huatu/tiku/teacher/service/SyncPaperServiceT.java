package com.huatu.tiku.teacher.service;

import com.huatu.tiku.TikuBaseTest;
import com.huatu.tiku.entity.teacher.PaperEntity;
import com.huatu.tiku.teacher.dao.mongo.OldPaperDao;
import com.huatu.tiku.teacher.dao.paper.PaperQuestionMapper;
import com.huatu.tiku.teacher.service.paper.PaperEntityService;
import com.huatu.ztk.paper.bean.Paper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by huangqingpeng on 2018/9/8.
 */
@Slf4j
public class SyncPaperServiceT extends TikuBaseTest{

    @Autowired
    OldPaperDao oldPaperDao;

    @Autowired
    PaperEntityService paperEntityService;

    @Autowired
    PaperQuestionMapper paperQuestionMapper;

    @Autowired
    RedisTemplate redisTemplate;

    public final static String vhuatu_name = "v_huatu_paper_name";
    public final static String vhuatu_count = "v_huatu_paper_count";
    public final static String pandora_name = "pandora_paper_name";
    public final static String pandora_count = "pandora_paper_count";
    public final static String mongo_name = "mongo_paper_name";
    public final static String mongo_count = "mongo_paper_count";

    @Test
    public void test(){
        HashOperations hashOperations = redisTemplate.opsForHash();
        Map entries = hashOperations.entries(vhuatu_name);
        if(null == entries){
            return;
        }
        List<PaperEntity> paperEntities = paperEntityService.selectAll();
        Map<String, String> name = paperEntities.stream().collect(Collectors.toMap(i -> String.valueOf(i.getSubjectId()+"_"+i.getId()), i -> i.getName()));
        List<Map> maps = paperQuestionMapper.groupByCount();
        Map<String, String> countMap = maps.stream().collect(Collectors.toMap(i -> String.valueOf(i.get("id")), i -> String.valueOf(i.get("total"))));
        redisTemplate.opsForHash().putAll(pandora_name,name);
        redisTemplate.opsForHash().putAll(pandora_count,countMap);
        List<Paper> all = oldPaperDao.findAll();
        Map<String, String> collect = all.stream().collect(Collectors.toMap(i -> String.valueOf(i.getCatgory() + "_" + i.getId()), i -> i.getName()));
        Map<String, String> collect1 = all.stream().collect(Collectors.toMap(i -> String.valueOf(i.getId()), i -> {
            List<Integer> questions = i.getQuestions();
            if(CollectionUtils.isEmpty(questions)){
                return "0";
            }
            return i.getQuestions().size()+"";
        }));
        redisTemplate.opsForHash().putAll(mongo_name,collect);
        redisTemplate.opsForHash().putAll(mongo_count,collect1);

    }
}
