package com.huatu.ztk.user.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.huatu.ztk.user.daoPandora.IconMapper;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-10-25 3:32 PM
 **/

@Service
@Slf4j
public class IconService {


    @Autowired
    private UserServerConfig userServerConfig;


    private static final Cache<Integer, List<IconCache>> cache = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.SECONDS).build();

    @Autowired
    private IconMapper iconMapper;

    public Object list(int subject){
        try{
            return cache.get(subject, new Callable<List<IconCache>>() {
                @Override
                public List<IconCache> call() throws Exception {
                    log.info("request data from cache failed:{}", subject);
                    return dataBaseIcons(subject);
                }
            });
        }catch (ExecutionException e){
            log.error("load subject icon config error:{} message :{}", subject, e.getMessage());
            return defaultIcons();
        }
    }

    /**
     * 数据库加载 icon 配置
     * @param subject
     * @return
     */
    private List<IconCache> dataBaseIcons(int subject){
        log.info("load icons from data base:{}", subject);
        List<IconCache> result = Lists.newArrayList();
        try{
            List<HashMap<String, Object>> list = iconMapper.list(subject);
            log.info("icon config from database:{}", list);
            if(CollectionUtils.isEmpty(list)){
                return defaultIcons();
            }
            for (HashMap<String, Object> temp : list) {
                String type = MapUtils.getString(temp, "type" );
                String name = MapUtils.getString(temp, "name");
                String url = MapUtils.getString(temp, "url");
                Integer sort = MapUtils.getInteger(temp, "sort");
                IconCache iconCache = IconCache.builder().type(type).name(name).url(url).sort(sort).build();
                result.add(iconCache);
            }
            log.info("icon database result:{}", JSONObject.toJSONString(result));
            return result;
        }catch (Exception e){
            e.printStackTrace();
            log.error("error:{}", e);
            return defaultIcons();
        }
    }


    /**
     * 返回默认的 icons 配置
     * @return
     */
    private List<IconCache> defaultIcons(){
        List<IconCache> defaultList = JSONArray.parseArray(userServerConfig.getDefaultIcons(), IconCache.class);
        return defaultList;
    }


    @NoArgsConstructor
    @Setter
    @Getter
    private static class IconCache{

        private String type;

        private String name;

        private String url;

        private Integer sort;

        @Builder
        public IconCache(String type, String name, String url, Integer sort) {
            this.type = type;
            this.name = name;
            this.url = url;
            this.sort = sort;
        }
    }
}
