package com.huatu.ztk.search.observer;

import com.huatu.ztk.search.bean.KeyWordSearchBeanNew;
import com.huatu.ztk.search.hystrix.CommendHotWord;
import com.huatu.ztk.search.util.RedisCacheUpdate;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * @author zhengyi
 * @date 2019-03-07 16:31
 **/
@Component
public class HotWordObserverRedis implements Observer {

    @Autowired
    private RedisCacheUpdate redisCacheUpdate;

    @Override
    public void update(Observable o, Object arg) {
        KeyWordSearchBeanNew keyWordSearchBeanNew = (KeyWordSearchBeanNew) arg;
        switch (keyWordSearchBeanNew.getOption()) {
            case INSERT:
                gogogo(keyWordSearchBeanNew);
                break;
            case UPDATE:
                gogogo(keyWordSearchBeanNew);
                break;
            case DELETE:
                gogogo(keyWordSearchBeanNew);
            default:
        }
    }

    private boolean gogogo(KeyWordSearchBeanNew keyWordSearchBeanNew) {
        HystrixRequestContext context = HystrixRequestContext.initializeContext();
        CommendHotWord commendHotWord = new CommendHotWord(keyWordSearchBeanNew.getUid(), keyWordSearchBeanNew.getCatgory());
        boolean responseFromFallback = commendHotWord.isResponseFromFallback();
        if (responseFromFallback) {
            context.close();
            return true;
        } else {
            try {
                redisCacheUpdate.updateValue(keyWordSearchBeanNew.getRedisKey(), commendHotWord.queue().get().stream().map(x -> x + ",").collect(Collectors.joining()));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        context.close();
        return false;
    }
}