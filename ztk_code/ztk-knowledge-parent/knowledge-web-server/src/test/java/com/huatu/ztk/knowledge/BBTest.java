package com.huatu.ztk.knowledge;

import com.google.common.cache.Cache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.huatu.ztk.commons.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.mapping.CacheBuilder;
import org.junit.internal.runners.statements.RunAfters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by shaojieyue
 * Created time 2016-07-22 13:11
 */
public class BBTest {
    private static final Logger logger = LoggerFactory.getLogger(BBTest.class);

    public static Cache<Integer, Integer> cache = com.google.common.cache.CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).build();

    public static ReentrantLock lock = new ReentrantLock();

    public static void main(String[] args) throws InterruptedException {
//        cache.put(1,1);
//        Map<Integer,Integer> map = Maps.newHashMap();
//        map.put(1,1);
//        Thread.sleep(5000);
//        System.out.println("JsonUtil.t = " + JsonUtil.toJson(cache.asMap()));
//        cache.putAll(map);
//        System.out.println("JsonUtil.t = " + JsonUtil.toJson(cache.asMap()));
//        Thread.sleep(5000);
//        cache.putAll(map);
//        System.out.println("JsonUtil.t = " + JsonUtil.toJson(cache.asMap()));
//        Thread.sleep(5000);
//        System.out.println("JsonUtil.t = " + JsonUtil.toJson(cache.asMap()));
//        Thread.sleep(1000);
//        System.out.println("JsonUtil.t = " + JsonUtil.toJson(cache.asMap()));
        test();
        new Thread(BBTest::test).start();
        new Thread(BBTest::test).start();
        new Thread(BBTest::test).start();
        new Thread(BBTest::test).start();
        new Thread(BBTest::test).start();
        new Thread(BBTest::test).start();
    }

    private static void test() {


        try {
            Runnable runnable = new Runnable() {

                @Override
                public void run() {
                    boolean b = false;
                    try {
                        b = lock.tryLock(1, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {

                        if (b) {
                            System.out.println("获取到锁 ：Thread.currentThread().getName() = " + Thread.currentThread().getName());

                            Thread.sleep(10000);
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        if (b) {
                            System.out.println("释放锁 ：Thread.currentThread().getName() = " + Thread.currentThread().getName());

                            lock.unlock();
                        }

                    }
                }
            };
            Thread thread = new Thread(runnable);
            int i = ThreadLocalRandom.current().nextInt(2);
            System.out.println("i = " + i);
            if (i == 1) {
                thread.run();
            } else {
                thread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
