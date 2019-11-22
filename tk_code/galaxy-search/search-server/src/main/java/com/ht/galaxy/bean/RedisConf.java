package com.ht.galaxy.bean;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;

import java.util.HashSet;
import java.util.Set;

public class RedisConf {

    private static JedisSentinelPool jedisSentinelPool = null;

    private RedisConf() {}

    public static void main(String[] args) {

        JedisSentinelPool jedisPool = getJedisSentinelPool();
        Jedis jedis = jedisPool.getResource();

        System.out.println(jedis.get("mode"));
        System.out.println(jedis.get("key"));
        System.out.println(jedis.get("sql"));
        jedis.close();

    }

    //单例连接池
    public static JedisSentinelPool getJedisSentinelPool() {

        if (jedisSentinelPool == null) {
            synchronized (RedisConf.class) {
                if (jedisSentinelPool == null) {
                    Set<String> sentinels = new HashSet<String>();
                    String hostAndPort1 = "192.168.100.20:26489";
                    String hostAndPort2 = "192.168.100.20:26489";
                    sentinels.add(hostAndPort1);
                    sentinels.add(hostAndPort2);
                    String clusterName = "user-session-master";

                    JedisPoolConfig poolConfig = new JedisPoolConfig();
                    poolConfig.setTestOnBorrow(true);
                    // 最大连接数
                    poolConfig.setMaxTotal(3);
                    // 最大空闲数
                    poolConfig.setMaxIdle(3);
                    poolConfig.setMinIdle(1);
                    // 最大允许等待时间，如果超过这个时间还未获取到连接，则会报JedisException异常：
                    // Could not get a resource from the pool
                    try {
                        poolConfig.setMaxWaitMillis(5000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    jedisSentinelPool = new JedisSentinelPool(clusterName, sentinels, poolConfig);
                }
            }
        }
        return jedisSentinelPool;
    }
//    public static JedisCluster cluster(){
//        JedisPoolConfig poolConfig = new JedisPoolConfig();
//        poolConfig.setTestOnBorrow(true);
//        // 最大连接数
//        poolConfig.setMaxTotal(105);
//        // 最大空闲数
//        poolConfig.setMaxIdle(10);
//        poolConfig.setMinIdle(1);
//        // 最大允许等待时间，如果超过这个时间还未获取到连接，则会报JedisException异常：
//        // Could not get a resource from the pool
//        poolConfig.setMaxWaitMillis(5000);
//
//        Set<HostAndPort> nodes = new LinkedHashSet<HostAndPort>();
//        nodes.add(new HostAndPort("192.168.100.110", 16381));
//        nodes.add(new HostAndPort("192.168.100.110", 6380));
//        nodes.add(new HostAndPort("192.168.100.111", 16381));
//        nodes.add(new HostAndPort("192.168.100.111", 6380));
//        nodes.add(new HostAndPort("192.168.100.112", 6379));
//        nodes.add(new HostAndPort("192.168.100.112", 6380));
//
//        JedisCluster cluster = new JedisCluster(nodes, poolConfig);
//        return cluster;
//    }


}


