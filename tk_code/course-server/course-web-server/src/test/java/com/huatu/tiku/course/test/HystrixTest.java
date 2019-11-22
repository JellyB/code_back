package com.huatu.tiku.course.test;

import com.google.common.collect.ImmutableMap;
import com.netflix.hystrix.*;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author hanchao
 * @date 2017/10/1 9:14
 */
public class HystrixTest extends HystrixCommand<Map>{
    protected HystrixTest(Setter setter) {
        super(setter);
    }

    public static void main(String[] args){
        HystrixCommandGroupKey hystrixCommandGroupKey = HystrixCommandGroupKey.Factory.asKey("test-group");
        HystrixCommandKey hystrixCommandKey = HystrixCommandKey.Factory.asKey("test-method");
        HystrixThreadPoolKey threadPoolKey = HystrixThreadPoolKey.Factory.asKey("test-threadpool-key");
        HystrixThreadPoolProperties.Setter threadPoolProperties = HystrixThreadPoolProperties.Setter()
                .withCoreSize(5)
                .withKeepAliveTimeMinutes(5)
                .withMaximumSize(100)
                .withMaxQueueSize(Integer.MAX_VALUE);
        HystrixCommandProperties.Setter hystrixCommandPropertiesSetter = HystrixCommandProperties.Setter()
                .withExecutionTimeoutInMilliseconds(2000) // 默认超时时间,默认是1000,可以调小测试
                .withCircuitBreakerEnabled(true) //是否开启熔断机制，默认为true。
                .withCircuitBreakerForceClosed(false) //是否强制关闭熔断开关，如果强制关闭了熔断开关，则请求不会被降级，一些特殊场景可以动态配置该开关，默认为false。
                .withCircuitBreakerForceOpen(false) //是否强制打开熔断开关，如果强制打开可熔断开关，则请求强制降级调用getFallback处理，可以通过动态配置来打开该开关实现一些特殊需求，默认为false。
                .withCircuitBreakerErrorThresholdPercentage(50) //如果在一个采样时间窗口内，失败率超过该配置，则自动打开熔断开关实现降级处理，即快速失败。默认配置下采样周期为10s，失败率为50%。
                .withCircuitBreakerRequestVolumeThreshold(20) //在熔断开关闭合情况下，在进行失败率判断之前，一个采样周期内必须进行至少N个请求才能进行采样统计，目的是有足够的采样使得失败率计算正确，默认为20。
                .withCircuitBreakerSleepWindowInMilliseconds(5000) //熔断后的重试时间窗口，且在该时间窗口内只允许一次重试。即在熔断开关打开后，在该时间窗口允许有一次重试，如果重试成功，则将重置Health采样统计并闭合熔断开关实现快速恢复，否则熔断开关还是打开状态，执行快速失败。
                .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.THREAD); //使用thread隔离策略

        HystrixCommand.Setter setter = HystrixCommand.Setter
                .withGroupKey(hystrixCommandGroupKey)
                .andCommandKey(hystrixCommandKey)
                .andThreadPoolKey(threadPoolKey)
                .andThreadPoolPropertiesDefaults(threadPoolProperties)
                .andCommandPropertiesDefaults(hystrixCommandPropertiesSetter);


        /**
         * 可以调低失败率测试
         */
        for (int i = 0; i < 10; i++) {
            new Thread(){
                @Override
                public void run() {
                    for (int i1 = 0; i1 < 100; i1++) {
                        HystrixTest test = new HystrixTest(setter);
                        Map m = test.execute();
                        System.out.println(m);
                    }
                }
            }.start();

        }

    }

    @Override
    protected Map run() throws Exception {
        System.out.println(Thread.currentThread());
        TimeUnit.MILLISECONDS.sleep(new Random().nextInt(500)+1000);
        return ImmutableMap.of("a","哈德","b","阿斯顿");
    }


    @Override
    protected Map getFallback() {
        return ImmutableMap.of("a","假数据");
    }

}
