package com.huatu.ztk.paper.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by shaojieyue
 * Created time 2016-07-13 15:24
 */
public class RateTest{
    public static void main(String[] args) {
        String ss ="68114,40311,38331,39366,39950,39356,72895,40304,40279,59244,77709,38691,72865,39253,75678,40268,72880,40279,42757,42515";
        final String[] strings = ss.split(",");
        System.out.println(strings.length);
        Set<String> set = Sets.newHashSet();
        for (String string : strings) {
            if (set.contains(string)) {
                System.out.println("-->"+string);
            }
            set.add(string);
        }
    }



}
