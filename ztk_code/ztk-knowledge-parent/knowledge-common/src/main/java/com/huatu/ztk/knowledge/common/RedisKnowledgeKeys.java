package com.huatu.ztk.knowledge.common;

/**
 * 知识点的redis keys 列表
 * Created by shaojieyue
 * Created time 2016-06-13 13:46
 */
public class RedisKnowledgeKeys {

    /**
     * 已完成试题列表 finish_用户id_科目id_知识点
     * @param uid 用户id
     * @param point 知识点
     * @return
     */
    public static String getFinishedSetKey(long uid, int point){
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("finish_").append(uid)
                .append("_1")
                .append("_").append(point);
        return stringBuilder.toString();
    }

    /**
     * 获取知识点完成试题个数key
     * @param uid 用户id
     * @return
     */
    public static final String getFinishedCountKey(long uid){
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("finish_count_").append(uid)
                .append("_1");
        return stringBuilder.toString();
    }

    /**
     * 已完成知识点列表 finish_point_用户id_科目id
     * @return
     */
    public static String getFinishedPointKey(long uid){
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("finish_point_").append(uid)
                .append("_1");
        return stringBuilder.toString();
    }

    /**
     * 已完成知识点列表 finish_smart_用户id
     * @return
     */
    public static String getFinishedSmartKey(long uid,int subject){
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("subject_").append(subject).append("_finish_smart_").append(uid)
                .append("_1");
        return stringBuilder.toString();
    }

    /**
     * 错误试题列表
     * @param uid 用户id
     * @param point 知识点
     * @return
     */
    public static final String getWrongSetKey(long uid, int point){
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("wrong_").append(uid)
                .append("_1")
                .append("_").append(point);
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
                .append("_1")
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
        stringBuilder.append("wrong_count_").append(uid)
                .append("_1");
        return stringBuilder.toString();
    }

    /**
     * 收藏试题列表
     * @param uid 用户id
     * @param point 知识点
     * @return
     */
    public static final String getCollectSetKey(long uid,int point){
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("collect_").append(uid)
                .append("_1")
                .append("_").append(point);
        return stringBuilder.toString();
    }

    /**
     * 收藏知识点错误试题个数key
     * @param uid 用户id
     * @return
     */
    public static final String getCollectCountKey(long uid){
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("collect_count_").append(uid)
                .append("_1");
        return stringBuilder.toString();
    }

    /**
     * 试题对象id
     * @param qid
     * @return
     */
    public static final String getQuestionIdKey(int qid){
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("question_").append(qid);
        return stringBuilder.toString();
    }

    /**
     * 知识点对应的试题id列表
     * @param pointId
     * @return
     */
    public static final String getPointQuesionIds(int pointId){
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("point_qids_").append(pointId);
        return stringBuilder.toString();
    }

    /**
     * 年份、模块对应的试题id列表
     * @param year
     * @param moduleId
     * @param subject
     * @return
     */
    public static final String getYearModuleQuestions(int year,int moduleId,int subject){
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("subject_").append(subject).append("_year_").append(year).append("_module_").append(moduleId);
        return stringBuilder.toString();
    }

    /**
     * 年份、模块对应的试题id列表
     * @param year
     * @param moduleId
     * @param subject
     * @return
     */
    public static final String getYearModuleQuestionsV3(int year,int moduleId,int subject){
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("subject_V3_").append(subject).append("_year_").append(year).append("_module_").append(moduleId);
        return stringBuilder.toString();
    }

    /**
     * 年份、模块对应的试题id列表
     * @param year
     * @param moduleId
     * @param subject
     * @return
     */
    public static final String getYearModuleQuestions2(int year,int moduleId,int subject){
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("new_subject_").append(subject).append("_year_").append(year).append("_module_").append(moduleId);
        return stringBuilder.toString();
    }

    /**
     * 知识点-试题个数 列表
     * @return
     */
    public static final String getPointSummaryKey(){
        return "point_question_id";
    }

    /**
     * 未完成的专项练习 list,也可以用score是时间的zset
     *
     * @param uid
     * @param subject
     * @return
     */
    public static final String getUnfinishedPointListKey(long uid,int subject) {
        return new StringBuilder("unfinished_point_list_")
                .append(uid).append("_").append(subject).toString();
    }
    
    /**
     * 未完成的专项练习 list 区别背题模式的答题卡
     * @param uid
     * @param subject
     * @param modeEnumKey
     * @return
     */
	public static final String getUnfinishedPointListKeyV2(long uid, int subject, int modeEnumKey) {
		return new StringBuilder("unfinished_point_list_").append(uid).append("_").append(subject).append("_")
				.append(modeEnumKey).toString();
	}

    /**
     * 获得
     * @return
     */
    public static String getUserPointUpdateLockKey() {
        return "user_point_update_lock";
    }

    /**
     * 更新用户数据，处理上限
     * @return
     */
    public static String getUserPointUpdateIncrementKey() {
        return "user_point_update_increment";
    }

    public static String getWrongDownloadIds(long uid) {
        return "wrong_download_id_set_key" + uid;
    }

}
