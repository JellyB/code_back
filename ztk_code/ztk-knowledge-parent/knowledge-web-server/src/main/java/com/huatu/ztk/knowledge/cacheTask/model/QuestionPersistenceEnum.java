package com.huatu.ztk.knowledge.cacheTask.model;

/**
 * 试题记录 持久化 常量
 * Created by junli on 2018/3/19.
 */
public enum QuestionPersistenceEnum {
    ;
    public static final boolean CLEAN_DATA = true;
    /**
     * 缓存数据 对应的 mysql 数据表名称
     */
    public enum TableName{
        //用户已答试题列表
        QUESTION_USER_CACHE_FINISH("v_question_user_cache_finish"),
        //用户错误试题列表(错误次数多的 会排列在前面)
        QUESTION_USER_CACHE_WRONG("v_question_user_cache_wrong"),
        //用户收藏的试题列表
        QUESTION_USER_CACHE_COLLECT("v_question_user_cache_collect");

        private String tableName;
        TableName(String tableName) {
            this.tableName = tableName;
        }

        public String getTableName() {
            return tableName;
        }
    }

    /**
     * 用以记录每日需要更新的数据
     * redis 中使用set 缓存
     */
    public enum RedisKey{
        QUESTION_USER_CACHE_FINISH("question:user:cache:finish"),
        //用户错误试题列表(错误次数多的 会排列在前面)
        QUESTION_USER_CACHE_WRONG("question:user:cache:wrong"),
        //用户收藏的试题列表
        QUESTION_USER_CACHE_COLLECT("question:user:cache:collect");

        private String redisKey;

        RedisKey(String redisKey) {
            this.redisKey = redisKey;
        }

        public String getRedisKey() {
            return redisKey;
        }
    }

    /**
     * mysql 中数据状态
     */
    public enum DataState{
        IN_USING(1),
        UN_USING(0);

        private Integer state;

        DataState(Integer state) {
            this.state = state;
        }

        public Integer getState() {
            return state;
        }
    }

}
