package com.huatu.ztk.search.config;

import com.huatu.ztk.search.dao.HotwordDao;
import com.huatu.ztk.search.observer.HotWordObservable;
import com.huatu.ztk.search.observer.HotWordObserver;
import com.huatu.ztk.search.observer.HotWordObserverRedis;
import com.huatu.ztk.search.util.SpringTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * @author zhengyi
 * @date 2019-03-01 11:13
 **/
@Configuration
@Order
public class HotWordConfig {
    @Autowired
    private HotwordDao hotwordDao;
    @Autowired
    private HotWordObserver hotWordObserver;
    @Autowired
    private HotWordObserverRedis hotWordObserverRedis;

    @Bean
    public List<String> hotWordComponent() {
        return hotwordDao.query(1);
    }

    @Bean
    public HotWordObservable registerObserver() {

        HotWordObservable hotWordObservable = new HotWordObservable();
        hotWordObservable.addObserver(hotWordObserverRedis);
        hotWordObservable.addObserver(hotWordObserver);
        hotWordObservable.setChanged();
        return hotWordObservable;
    }

    @Bean
    public SpringTool springTool() {
        return new SpringTool();
    }
}