package com.huatu.ztk.search.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import static com.netflix.hystrix.HystrixCommand.Setter.withGroupKey;
import static com.netflix.hystrix.HystrixCommandGroupKey.Factory.asKey;

/**
 * @author zhengyi
 * @date 2019-03-07 15:35
 **/
@Getter
@Setter
public abstract class HotWordFactory extends HystrixCommand<List<String>> {
    protected long userId;
    protected int catgory;

    protected HotWordFactory(long userId, int catgory) {
        super(withGroupKey(asKey("jbzm-nb"))
                .andCommandKey(HystrixCommandKey.Factory.asKey("nb"))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        .withExecutionTimeoutEnabled(true)
                        .withExecutionTimeoutInMilliseconds(1000)));
        this.userId = userId;
        this.catgory = catgory;
    }
}