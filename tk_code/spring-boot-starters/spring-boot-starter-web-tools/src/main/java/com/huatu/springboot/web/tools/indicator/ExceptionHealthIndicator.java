package com.huatu.springboot.web.tools.indicator;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Maps;
import com.huatu.springboot.web.tools.exception.ExceptionCounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

import java.util.Collection;
import java.util.Map;

/**
 * @author hanchao
 * @date 2018/1/16 12:02
 */
public class ExceptionHealthIndicator extends AbstractHealthIndicator {
    public static final int FREQUENCY_THRESHOLD = 1;//平均每秒一个即认为异常

    @Autowired
    private ExceptionCounter exceptionCounter;

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        builder.up();
        int seconds = exceptionCounter.seconds();
        int count = exceptionCounter.count();
        int frequency = count / seconds;

        Map<String,Integer> statistics = Maps.newHashMap();
        Collection<Exception> samples = exceptionCounter.samples();
        HashMultiset<String> mSet = HashMultiset.create();
        samples.stream().forEach(e -> {
            mSet.add(e.getClass().getCanonicalName());
        });
        for (String e : mSet) {
            statistics.put(e,mSet.count(e));
        }


        builder.withDetail("count",count)
                .withDetail("seconds",seconds)
                .withDetail("frequency",frequency)
                .withDetail("recentErrors",statistics);
        if(frequency >= FREQUENCY_THRESHOLD){
            builder.down();
        }
    }
}
