package com.huatu.ztk.knowledge.common;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/6/11
 * @描述 错题redis  key
 */
public class RedisKnowledgeKeysV2 {

    /**
     * 错误试题列表
     * @param uid 用户id
     * @param point 知识点
     * @return
     */
    public static final String getWrongSetKey(long uid, int point){
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("wrong_").append(uid)
                .append("_").append(point);
        return stringBuilder.toString();
    }

    /**
     * 获取知识点错误试题个数key
     * @param uid 用户id
     * @return
     */
    public static final String getWrongCountKey(long uid){
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("wrong_count_").append(uid);
        return stringBuilder.toString();
    }

    /**
     * 错题背题模式游标
     * @param uid
     * @param point
     * @return
     */
    public static final String getWrongCursor(long uid,int point){
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("wrong_cursor").append(uid)
                .append("_").append(point);
        return stringBuilder.toString();
    }
    
    /**
     * 获取知识点完成数key
     * @param uid
     * @return
     */
    public static final String getFinishCountKey(long uid){
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("finish_count_").append(uid);
        return stringBuilder.toString();
    }

    /**
     * 已完成试题列表 finish_用户id_科目id_知识点
     * @param uid 用户id
     * @param point 知识点
     * @return
     */
    public static String getFinishedSetKey(long uid, int point){
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("finish_").append(uid)
                .append("_").append(point);
        return stringBuilder.toString();
    }
}
