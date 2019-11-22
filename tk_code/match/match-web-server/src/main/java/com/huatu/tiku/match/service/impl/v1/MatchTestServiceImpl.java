package com.huatu.tiku.match.service.impl.v1;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.huatu.tiku.match.bean.MatchTestBean;
import com.huatu.tiku.match.dao.manual.meta.MatchUserMetaMapper;
import com.huatu.tiku.match.service.MatchTestService;
import com.huatu.tiku.match.service.v1.meta.MatchUserMetaService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by huangqingpeng on 2019/3/1.
 */
@Service
@Slf4j
public class MatchTestServiceImpl implements MatchTestService {

    @Autowired
    MatchUserMetaService matchUserMetaService;
    @Autowired
    MatchUserMetaMapper matchUserMetaMapper;
    /**
     * 用以在本机缓存试题信息
     */
    private final static Cache<Integer, MatchTestBean> BASE_CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.DAYS)
            .maximumSize(2000)
            .build();
    private final static Cache<Integer, MatchTestBean> ENROLLED_CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.DAYS)
            .maximumSize(2000)
            .build();
    private final static Cache<Integer, MatchTestBean> CREATE_CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.DAYS)
            .maximumSize(2000)
            .build();
    private final static Cache<Integer, MatchTestBean> SAVED_CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.DAYS)
            .maximumSize(2000)
            .build();
    private final static Cache<Integer, MatchTestBean> SUBMIT_CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.DAYS)
            .maximumSize(2000)
            .build();
    public List<Integer> getUserIds() {
        List<Integer> userIds = matchUserMetaMapper.findUserIds();
        System.out.println("userIds = " + userIds);
        return userIds;
    }

    /**
     * 报名随机数
     * @return
     * @param base
     */
    public MatchTestBean randomTest(Operate base) {
        switch (base){
            case Base:
                return randomBase();
            case Enroll:
                return randomCache(ENROLLED_CACHE);
            case Create:
                return randomCache(CREATE_CACHE);
            case Save:
                return randomCache(SAVED_CACHE);
            case Submit:
                return randomCache(SUBMIT_CACHE);
        }
        return randomBase();
    }

    private MatchTestBean randomBase() {
        long size = BASE_CACHE.size();
        if (size == 0) {
            List<Integer> userIds = getUserIds();
            for (int i = 0; i < userIds.size(); i++) {
                BASE_CACHE.put(i,MatchTestBean.builder().userId(userIds.get(i))
                        .id(i)
                        .uname("").build());
            }
            size = userIds.size();
        }

        int random = ThreadLocalRandom.current().nextInt(1, new Long(size).intValue());
        MatchTestBean matchTestBean = BASE_CACHE.getIfPresent(random);
        return matchTestBean;
    }

    private MatchTestBean randomCache(Cache<Integer, MatchTestBean> baseCache) {
        ConcurrentMap<Integer, MatchTestBean> map = baseCache.asMap();
        if(map.size()==0){
            return null;
        }
        List<Map.Entry<Integer, MatchTestBean>> list = map.entrySet().stream().sorted(Comparator.comparingDouble(Map.Entry::getKey)).collect(Collectors.toList());
        int i = ThreadLocalRandom.current().nextInt(0, list.size());
        return list.get(i).getValue();
    }

    public void saveMatchTestBean(MatchTestBean matchTestBean,Operate operate){
        switch (operate){
            case Enroll:
                ENROLLED_CACHE.put(matchTestBean.getId(),matchTestBean);
            case Create:
                CREATE_CACHE.put(matchTestBean.getId(),matchTestBean);
            case Save:
                SAVED_CACHE.put(matchTestBean.getId(),matchTestBean);
            case Submit:
                SUBMIT_CACHE.put(matchTestBean.getId(),matchTestBean);
        }
        BASE_CACHE.put(matchTestBean.getId(),matchTestBean);
    }

    @AllArgsConstructor
    @Getter
    public enum Operate{
        Enroll(1,"报名"),
        Create(2,"创建答题卡"),
        Save(3,"保存"),
        Submit(4,"交卷"),
        Base(0,"初始化" );
        private int id;
        private String name;
    }
}
