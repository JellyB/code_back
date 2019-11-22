package com.huatu.common.consts;

/**
 * @author hanchao
 * @date 2017/10/17 18:26
 */
public class ApolloConfigConsts {
    public static final String NAMESPACE_DEFAULT = "";

    public static final String NAMESPACE_TIKU_BASIC = "tiku.basic";

    public static final String NAMESPACE_HT_METRICS = "htonline.metrics";

    public static final String NAMESPACE_HT_REDIS_QUEUE = "htonline.redis-queue";

    public static final String NAMESPACE_TIKU_RABBIT = "tiku.rabbitmq";

    public static final String NAMESPACE_TIKU_DB = "tiku.db";

    /**
     * 使用 redis 做持久化的 服务（模考大赛、课后作业、随堂练 etc 统计信息）
     */
    public static final String NAMESPACE_TIKU_REDIS = "tiku.redis-cluster";

    /**
     * 仅做缓存用的 redis 服务
     */
    public static final String NAMESPACE_TIKU_CACHE_REDIS = "tiku.redis-cache-cluster";

    public static final String NAMESPACE_TIKU_DUBBO = "tiku.dubbo";

    /**
     * 主数据库
     */
    public static final String NAMESPACE_DB_VHUATU = "tiku.db_vhuatu";

    /**
     * 从库
     */
    public static final String NAMESPACE_DB_VHUATU_SLAVE = "tiku.db_vhuatu_slave";

    public static final String NAMESPACE_TIKU_MONGO = "tiku.mongo-cluster";
}
