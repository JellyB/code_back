package com.huatu.ztk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

/**
 * Created by shaojieyue
 * Created time 2016-07-11 22:34
 */
public class JedisTest {
    private static final Logger logger = LoggerFactory.getLogger(JedisTest.class);

    public static void main(String[] args) {
        final Jedis jedis = new Jedis("192.168.100.22", 6379);
        final Jedis jedis80 = new Jedis("192.168.100.22", 6380);

    }
}
