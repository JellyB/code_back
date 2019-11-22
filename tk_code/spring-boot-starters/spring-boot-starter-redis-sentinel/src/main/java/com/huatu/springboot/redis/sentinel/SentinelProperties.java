package com.huatu.springboot.redis.sentinel;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static com.huatu.common.consts.ApolloConfigConsts.NAMESPACE_TIKU_CACHE_REDIS;


/**
 * sentinel pool
 */

@ConfigurationProperties(prefix = "spring.redis.sentinel")
@EnableApolloConfig(NAMESPACE_TIKU_CACHE_REDIS)
@Data
public class SentinelProperties {


    private Pool pool;

    private Sentinel sentinel;

    public Sentinel getSentinel() {
        return sentinel;
    }

    public void setSentinel(Sentinel sentinel) {
        this.sentinel = sentinel;
    }

    public Pool getPool() {
        return pool;
    }

    public void setPool(Pool pool) {
        this.pool = pool;
    }

    /**
     * Pool properties.
     */
    public static class Pool {

        /**
         * Max number of "idle" connections in the pool. Use a negative value to indicate
         * an unlimited number of idle connections.
         */
        private int maxIdle = 8;

        /**
         * Target for the minimum number of idle connections to maintain in the pool. This
         * setting only has an effect if it is positive.
         */
        private int minIdle = 0;

        /**
         * Max number of connections that can be allocated by the pool at a given time.
         * Use a negative value for no limit.
         */
        private int maxActive = 8;

        /**
         * Maximum amount of time (in milliseconds) a connection allocation should block
         * before throwing an exception when the pool is exhausted. Use a negative value
         * to block indefinitely.
         */
        private int maxWait = -1;

        private boolean testOnBorrow;

        public int getMaxIdle() {
            return this.maxIdle;
        }

        public void setMaxIdle(int maxIdle) {
            this.maxIdle = maxIdle;
        }

        public int getMinIdle() {
            return this.minIdle;
        }

        public void setMinIdle(int minIdle) {
            this.minIdle = minIdle;
        }

        public int getMaxActive() {
            return this.maxActive;
        }

        public void setMaxActive(int maxActive) {
            this.maxActive = maxActive;
        }

        public int getMaxWait() {
            return this.maxWait;
        }

        public void setMaxWait(int maxWait) {
            this.maxWait = maxWait;
        }

        public boolean isTestOnBorrow() {
            return testOnBorrow;
        }

        public void setTestOnBorrow(boolean testOnBorrow) {
            this.testOnBorrow = testOnBorrow;
        }
    }

    /**
     * Redis sentinel properties.
     */
    public static class Sentinel {

        /**
         * Name of Redis server.
         */
        private String master;

        /**
         * Comma-separated list of host:port pairs.
         */
        private String nodes;

        public String getMaster() {
            return this.master;
        }

        public void setMaster(String master) {
            this.master = master;
        }

        public String getNodes() {
            return this.nodes;
        }

        public void setNodes(String nodes) {
            this.nodes = nodes;
        }

    }
}
