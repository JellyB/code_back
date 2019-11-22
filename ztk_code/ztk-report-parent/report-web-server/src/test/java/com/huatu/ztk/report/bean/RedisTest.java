package com.huatu.ztk.report.bean;

import com.huatu.ztk.report.BaseTest;
import com.sohu.smc.comment.util.BinUtil;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by shaojieyue
 * Created time 2016-07-11 17:23
 */
public class RedisTest {

//    @Autowired
//    private RedisTemplate<String,String> redisTemplate;

    public static void main(String[] args) {
        Jedis jedis = new Jedis("localhost", 6379);
        jedis.flushDB();
        for (int i = 0; i < 10000; i++) {
//            aaTestByte(1000000+i,jedis);
//            aaTestSet(1000000+i,jedis);
              aaTestZSet(1000000+i,jedis);
        }
        System.out.println("处理完成");
    }

    public static void aaTestByte(long uid, Jedis jedis){


        for (int i = 0; i < 1000; i++) {
            byte[] bytes = new byte[500];
            BinUtil.putLong(bytes,uid,0);
            BinUtil.putInt(bytes,10000+i,8);
            jedis.set(bytes,new byte[]{1});
        }


    }

    public static void aaTestSet(long uid, Jedis jedis){
        final int len = 1000;
        String[] values = new String[len];
        for (int i = 0; i < len; i++) {
            values[i] = 10000+i+"";
        }
        jedis.lpush(uid+"",values);
    }

    public static void aaTestZSet(long uid, Jedis jedis){
        final int len = 130;
        Map values = new HashMap();
        for (int i = 0; i < len; i++) {
            values.put(10000+i+"",Double.valueOf(10000+i));
        }
        jedis.zadd(uid+"",values);
    }
}
