package com.huatu.tiku.match.common;


/**
 * 模考大赛信息
 * Created by lijun on 2018/10/17
 */
public final class MatchInfoRedisKeys {

    /**
     * 用户考试信息（报名信息+创建答题卡+分数+交卷信息等）缓存key
     */
    private static final String MATCH_USER_ENROLL_INFO_PREFIX = "ma.us.en.if.pr.%s.%s";
    /**
     * 用户报名地区hash(key)
     */
    private static final String MATCH_POSITION_ENROLL_HASH_KEY_PREFIX = "ma.po.en.ha.ke.pr.%s";
    /**
     * 用户报名学校hash(key)
     */
    private static final String MATCH_SCHOOL_ENROLL_HASH_KEY_PREFIX = "ma.sc.en.ha.ke.pr.%s";

    /**
     * 用户报名数据
     */
    private static final String MATCH_USER_META_LIST_AVAILALE_FOR_USER = "ma.us.me.li.av.fo.us.%s";
    /**
     * 模考大赛分数同步使用到的分布式锁
     */
    private static final String MATCH_SCORE_LOCK_KEY = "ma.sc.lo.ke.%s";

    /**
     * 全站最高分，在分数zset排序redis过时之后存储的缓存信息
     */
    private static final String MATCH_MAX_SCORE_KEY = "ma.ma.sc.ke.%s";

    /**
     * 总交卷人数（交卷人数，zset排序过时后的缓存信息）
     */
    private static final String MATCH_USER_SUBMIT_COUNT = "ma.us.su.co.%s";
    /**
     * 模考大赛用户排名缓存（zset过时之后的缓存信息）
     */
    private static final String MATCH_USER_RANK_HASH_KEY = "ma.us.ra.ha.ke.%s";
    /**
     * 模考大赛用户地区排名（zset过时之后的缓存信息）
     */
    private static final String MATCH_USER_POSITION_RANK_HASH_KEY = "ma.us.po.ra.ha.ke.%s.%s";
    /**
     * 模考大赛用户地区参与人数
     */
    private static final String MATCH_RANK_COUNT_FOR_POSITION_HASH_KEY = "ma.ra.co.fo.po.ha.ke.%s";
    /**
     * 模考大赛相关试题的统计信息持久化，分布式锁
     */
    private static final String MATCH_QUESTION_META_PERSISTENCE_LOCK_KEY = "ma.qu.me.pe.lo.ke";

    /**
     * 模考大赛首页查询可用模考列表缓存（key-value）
     */
    private static final String MATCH_HOME_PAGE_LIST_VALUE_KEY = "ma.ho.pa.li.va.ke.%s";
    /**
     * 模考大赛某个科目某个标签下的所有模考大赛（key-value）
     */
    private static final String MATCH_HOME_PAGE_LIST_VALUE_TAG_KEY = "ma.ho.pa.li.va.ta.ke.%s.%s";
    /**
     * 总体分数排名
     */
    private static final String MATCH_TOTAL_SCORE_ZSET_KEY = "ma.to.sc.zs.ke.%s";
    /**
     * 重置模考大赛试题统计数据的准备key(决定统计数据什么时候持久化入库)
     */
    private static final String QUESTION_META_LOCKED_MATCH_ID_KEY = "qu.me.lo.ma.id.ke";




    private static final String MATCH_PRACTICE_ID = "ma.es.pr.id.%s";
    private static final String PRACTICE_INFO = "pr.in";
    private static final String PUBLIC_USER = "pu.us.%s";
    private static final String MATCH_USER_PRACTICE_ID_PREFIX = "ma.us.pr.id.pr.%s";
    private static final String MATCH_USER_ESSAY_ENROLL_INFO_PREFIX = "ma.us.es.en.if.pr.%s.%s";

    /**
     * 模考大赛同步定时锁（key-value）对应正在处理的模考大赛ID
     */
    private static final String MATCH_USER_META_SYNC_LOCK = "ma.us.me.sy.lo";


    private static final String MATCH_SYNC_STATUS_HASH = "ma.sy.st.ha";


    /**
     * 模考大赛待同步试卷ID集合
     */
    private static final String MATCH_USER_META_SYNC_USER_ID_SET = "ma.us.me.sy.us.id.se.%s";
    private static final String MOCK_USER_ANSWER_STATUS_PREFIX = "essay-server.m_u_a_s_s_1228_%s";
    private static final String COURSE_ANALYSIS_INFO = "course_analysis_v6$%s";
    private static final String PAPER_GIFT_PACKAGE_INFO = "gift_package_$%s";
    private static final String GIFT_PACKAGE_COURSE_KEY = "gift_course_id$%s";
    private static final String MATCH_FINISH_FLAG = "match_finish_flag_$%s";
    private static final String Match_Submit_Answer_Card_Id_Set_Key = "match_submit_answer_card_id_set_key";
    
    /**
     * 模考首页数据定时刷新key
     */
    public static final String MATCH_INDEX_DATA_UPDATE_KEY = "match_index_data_update_key";


    public static String getMatchWhitUserReportKey() {
        return "match_white_key_2_0_1_9";
    }

    /**
     * 申论的答题卡创建set，答题卡提交后，答题卡会从其中移除
     *
     * @param paperId
     * @return
     */
    public static String getMatchPracticeIdSetKey(int paperId) {
        return String.format(MATCH_PRACTICE_ID, paperId);
    }


    public static String getPracticeInfoKey() {
        return PRACTICE_INFO;
    }

    public static String getPublicUserSetPrefix(long paperId) {
        return String.format(PUBLIC_USER, paperId);
    }

    /**
     * 用户答题卡状态(1未完成2已交卷3已批改)
     *
     * @param paperId
     * @return
     */
    public static String getEssayUserAnswerStatusKey(long paperId) {
        return String.format(MOCK_USER_ANSWER_STATUS_PREFIX, paperId);
    }

    /**
     * 模考大赛用户ID和创建的答题卡ID（HASH）
     *
     * @param paperId
     * @return
     */
    public static String getMatchPracticeIdKey(int paperId) {
        return String.format(MATCH_USER_PRACTICE_ID_PREFIX, paperId);
    }

    /**
     * 报名数据缓存
     *
     * @param matchId
     * @param userId
     * @return
     */
    public static String getUserEnrollHashKey(int matchId, int userId) {
        return String.format(MATCH_USER_ENROLL_INFO_PREFIX, matchId, userId);
    }

    public static String getUserEssayEnrollHashKey(Long essayPaperId, Integer userId) {
        return String.format(MATCH_USER_ESSAY_ENROLL_INFO_PREFIX, essayPaperId, userId);
    }

    /**
     * 用户报名数据（用户ID-报名地区ID）
     *
     * @param matchId
     * @return
     */
    public static String getMatchPositionEnrollHashKey(Integer matchId) {
        return String.format(MATCH_POSITION_ENROLL_HASH_KEY_PREFIX, matchId);
    }

    public static String getMatchSchoolEnrollHashKey(Integer matchId) {
        return String.format(MATCH_SCHOOL_ENROLL_HASH_KEY_PREFIX, matchId);
    }

    public static String getAvailableMatchUserMetaKey(int userId) {
        return String.format(MATCH_USER_META_LIST_AVAILALE_FOR_USER, userId);
    }

    /**
     * 分数同步-分布式锁
     *
     * @param paperId
     * @return
     */
    public static String getMatchScoreLockKey(int paperId) {
        return String.format(MATCH_SCORE_LOCK_KEY, paperId);
    }

    /**
     * 模考全站最高分key
     *
     * @param paperId
     * @return
     */
    public static String getUserMaxScoreKey(int paperId) {
        return String.format(MATCH_MAX_SCORE_KEY, paperId);
    }

    /**
     * 总交卷人数
     *
     * @param paperId
     * @return
     */
    public static String getUserSubmitCount(int paperId) {
        return String.format(MATCH_USER_SUBMIT_COUNT, paperId);
    }

    /**
     * 全站排名（后期缓存）
     *
     * @param paperId
     * @return
     */
    public static String getMatchUserRankHashKey(int paperId) {
        return String.format(MATCH_USER_RANK_HASH_KEY, paperId);
    }

    /**
     * 地区排名
     *
     * @param paperId
     * @param positionId
     * @return
     */
    public static String getMatchUserPositionRankHashKey(int paperId, Integer positionId) {
        return String.format(MATCH_USER_POSITION_RANK_HASH_KEY, paperId, positionId);
    }

    public static String getRankCountForPositionHashKey(int paperId) {
        return String.format(MATCH_RANK_COUNT_FOR_POSITION_HASH_KEY, paperId);
    }

    public static String getMatchQuestionMetaLockKey() {
        return MATCH_QUESTION_META_PERSISTENCE_LOCK_KEY;
    }

    /**
     * 首页列表查询缓存
     *
     * @param subject
     * @return
     */
    public static String getMatchListKey(int subject) {
        return String.format(MATCH_HOME_PAGE_LIST_VALUE_KEY, subject);

    }

    /**
     * 模考解析课信息缓存
     *
     * @param classId
     * @return
     */
    public static String getCourseAnalysisInfo(int classId) {
        return String.format(COURSE_ANALYSIS_INFO, String.valueOf(classId));
    }

    /**
     * paper配置的大礼包信息
     * @param paperId
     * @return
     */
    public static String getPaperGiftPackageInfo(int paperId){
        return String.format(PAPER_GIFT_PACKAGE_INFO, String.valueOf(paperId));
    }

    public static String getMatchListByTagKey(int subjectId, int tagId) {
        return String.format(MATCH_HOME_PAGE_LIST_VALUE_TAG_KEY, subjectId, tagId);

    }

    public static String getGiftPackageCourseKey(String courseId){
        return String.format(GIFT_PACKAGE_COURSE_KEY, courseId);
    }

    /**
     * 模考大赛同步状态缓存
     * @return
     */
    public static String getMatchSyncStatusHashKey(){
        return MATCH_SYNC_STATUS_HASH;
    }

    public static String getSyncMatchKey() {
        return MATCH_USER_META_SYNC_LOCK;
    }

    /**
     * 某个模考大赛待同步的用户ID
     * @param match
     * @return
     */
    public static String getSyncUserIdSetKey(int match) {
        return String.format(MATCH_USER_META_SYNC_USER_ID_SET,match);
    }

    /**
     * 试题统计数据
     * @param qid
     * @return
     */
    public static final String getQuestionMetaKey(int qid) {
        return "qmeta_" + qid;
    }


    /**
     * 未准备同步试题统计信息的模考试卷id集合
     * @return
     */
    public static String getQuestionMetaLockedMatchId() {
        return QUESTION_META_LOCKED_MATCH_ID_KEY;
    }

    /**
     * 查询是否完成的标识
     * @param paperId
     * @return
     */
    public static String getMatchFinishFlag(int paperId) {
        return String.format(MATCH_FINISH_FLAG,paperId);
    }

    /**
     * 所有客户端交卷待处理答题卡ID集合缓存（先自动交卷定时任务处理）
     * @return
     */
    public static String getMatchSubmitAnswerCardIdSetKey() {
        return Match_Submit_Answer_Card_Id_Set_Key;
    }
}
