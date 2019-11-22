package com.huatu.tiku.essay.constant.cache;


import com.google.common.base.Joiner;

/**
 * @author zhouwei
 * @Description: 申论相关缓存key
 * @create 2017-12-16 下午2:15
 **/
public class RedisKeyConstant {





    /*  永久缓存 修改后可手动删除   */
    /**
     * !!! key 中拼接1228是为了避免key重复 !!!
     */
    /*  题目类型  */
    public static String SINGLE_QUESTION_TYPE_PREFIX = "s_q_t";
    /*  单题下材料列表  */
    public static String SINGLE_QUESTION_MATERIAL_PREFIX = "s_q_m";

    /*  单题列表缓存  1分钟过期 */
    public static String SINGLE_QUESTION_PREFIX = "s_q_p_2018329_zw";
    /**
     * 申论端写入的缓存
     * m :mock
     * e:exam
     * 模考相关的缓存都用me:开头，文件夹的形式
     * <p>
     * 缓存 10分钟过期  所有考完试的模考id列表
     */
    public static String MOCK_FINISHED_EXAM_ID_LIST = "me:i_20180402_zw";


    /* 模考的基本信息 */
    public static String MOCK_DETAIL_PREFIX = "m_d_1228";
    /*  模考试卷下材料列表  */
    public static String MOCK_PAPER_MATERIAL_PREFIX = "m_p_m_1228";
    /*  模考试卷下题目  */
    public static String MOCK_PAPER_QUESTION_PREFIX = "m_p_q_1228";
    /*  模考试卷基本信息  */
    public static String MOCK_PAPER_BASE_PREFIX = "m_p_b_1228";
    /*  用户答题卡状态 （状态） 行测端使用*/
    public static String MOCK_USER_ANSWER_STATUS_PREFIX = "m_u_a_s_s_1228";
    /*  用户模考答题卡信息 （答案。作答时间等）（批改用） */
    public static String MOCK_EXAM_ANSWER_PREFIX = "m_e_a_1228";
    /* 模考历史数据 */
    public static String MOCK_EXAM_HISTORY_PREFIX = "m_e_h_1228";
    /* 模考折线数据 */
    public static String MOCK_EXAM_LINE_PREFIX = "m_e_l_1228";
    /* 模考的的成绩信息 （平均分，考试总人数） */
    public static String MOCK_PAPER_SCORE_PREFIX = "m_p_a_s_1228";
    /* 最近的一次申论模考id（每次考试结束写入） */
    public static String LAST_ESSAY_MOCK_ID = "l_e_m_o_1228";


    /* 模考的报告 */
    public static String MOCK_EXAM_REPORT_PREFIX = "m_e_r_1228";

    /* 模考USER 的set*/
    public static String MOCK_USER_SET_PREFIX = "m_u_s_1228";
    /* 模考 自动交卷锁*/
    public static String MOCK_AUTO_SUBMIT_LOCK = "m_a_s_l";


    /* 申论模考的成绩Zset 无地区*/
    public static String ESSAY_USER_SCORE_PREFIX = "e_u_s_p_1228";
    /* 申论模考的成绩Zset 有地区*/
    public static String ESSAY_USER_AREA_SCORE_PREFIX = "e_u_a_s_p_1228";
    /* 申论成绩  求和 */
    public static String ESSAY_SCORE_SUM_PREFIX = "e_s_1228";


    /* 模考的总成绩Zset 无地区*/
    public static String MOCK_USER_TOTAL_SCORE_PREFIX = "t_u_s_p_1228";
    /* 模考的总成绩Zset 有地区*/
    public static String MOCK_USER_AREA_TOTAL_SCORE_PREFIX = "t_u_a_s_p_1228";
    /* 模考成绩求和 */
    public static String MOCK_SCORE_SUM_PREFIX = "m_s_s_1228";


    /**
     * 申论端读取的缓存
     */
    /* 用户参加模考的地区信息 （行测  提供）*/
    public static String MOCK_USER_AREA_PREFIX = "e_p_u_1228";

    /* 用户行测成绩的zSet*/
    public static String USER_PRACTICE_SCORE_PREFIX = "u_p_s_1228";
    /* 三方公用的userId的set （用户交卷时存入，批改完成后移除，行测端读取）*/
    public static String PUBLIC_USER_SET_PREFIX = "p_u_1228";
    /*用户报名人数*/
    public static String MOCK_ENROLL_COUNT_PAPER = "e_c_p";
    /*用户地区报名划分*/
    public static String MOCK_ENROLL_COUNT_PAPER_POSITION = "e_c_p_p";

    //批改是否免费
    public static final String ESSAY_GOODS_FREE_KEY = "essay_goods_free";
    //是否支持语音答题
    public static final String VOICE_ANSWER_KEY = "voice_answer";
    //是否支持拍照答题
    public static final String PHOTO_ANSWER_KEY = "photo_answer";


    //用户题目收藏
    public static final String ESSAY_USER_COLLECTION = "user_collection";
    public static final String USER_MOCK_ANSWER_LIST = "user_mock_answer_list";

    /**
     * 估分试卷列表
     *
     * @return
     */
    public static String getEstimatePaperListKey() {
        return "essay_estimate_paper_list";
    }


    /**
     * 用户收藏题目信息
     *
     * @return
     */
    public static String getUserCollectionSetKey(int type, int userId) {
        return Joiner.on("_").join(RedisKeyConstant.ESSAY_USER_COLLECTION, type, userId);
    }

    /**
     * 单题列表题目信息
     *
     * @return
     */
    public static String getSingleQuestionPrefix(int type, int page, int pageSize) {
        return Joiner.on("_").join(RedisKeyConstant.SINGLE_QUESTION_PREFIX, type, page, pageSize);
    }

    /**
     * 单题列表题目信息
     *
     * @return
     */
    public static String getNewSingleQuestionPrefix(int type, int page, int pageSize) {
        return Joiner.on("_").join(RedisKeyConstant.SINGLE_QUESTION_PREFIX, "new", type, page, pageSize);
    }

    /**
     * 三方共用的userId的set(申论写入，批改删除，行测读取)
     *
     * @return
     */
    public static String getPublicUserSetPrefix(long paperId) {
        return Joiner.on("_").join(RedisKeyConstant.PUBLIC_USER_SET_PREFIX, paperId);
    }

    /**
     * 模考信息
     *
     * @return
     */
    public static String getMockDetailPrefix(long paperId) {
        return Joiner.on("_").join(RedisKeyConstant.MOCK_DETAIL_PREFIX, paperId);
    }

    /**
     * 试卷信息
     *
     * @param paperId
     * @return
     */
    public static String getPaperBaseKey(long paperId) {
        return Joiner.on("_").join(RedisKeyConstant.MOCK_PAPER_BASE_PREFIX, paperId);
    }

    /**
     * 最近一次申论模考的id
     *
     * @return
     */
    public static String getLastEssayMockIdKey() {
        return RedisKeyConstant.LAST_ESSAY_MOCK_ID;
    }


    /**
     * 试题材料列表
     *
     * @param questionBaseId
     * @return
     */
    public static String getSingleQuestionMaterialKey(long questionBaseId) {
        return Joiner.on("_").join(RedisKeyConstant.SINGLE_QUESTION_MATERIAL_PREFIX, questionBaseId);
    }

    /**
     * 试卷材料列表
     *
     * @param paperId
     * @return
     */
    public static String getPaperMaterialKey(long paperId) {
        return Joiner.on("_").join(RedisKeyConstant.MOCK_PAPER_MATERIAL_PREFIX, paperId);
    }


    /**
     * 试卷问题列表
     *
     * @param paperId
     * @return
     */
    public static String getPaperQuestionKey(long paperId) {
        return Joiner.on("_").join(RedisKeyConstant.MOCK_PAPER_QUESTION_PREFIX, paperId);
    }


    /**
     * 用户答题卡状态
     *
     * @param paperId
     * @return
     */
    public static String getUserAnswerStatusKey(long paperId) {
        return Joiner.on("_").join(RedisKeyConstant.MOCK_USER_ANSWER_STATUS_PREFIX, paperId);
    }


    /**
     * 试卷答题卡
     *
     * @param paperId
     * @param userId
     * @return
     */
    public static String getExamAnswerKey(long paperId, int userId) {
        return Joiner.on("_").join(RedisKeyConstant.MOCK_EXAM_ANSWER_PREFIX, paperId, userId);
    }


    /**
     * 模考历史数据
     *
     * @param userId
     * @return
     */
    public static String getMockHistoryKey(long userId, long paperId) {
        return Joiner.on("_").join(RedisKeyConstant.MOCK_EXAM_HISTORY_PREFIX, userId, paperId);
    }


    /**
     * 模考历史数据
     *
     * @param userId
     * @return
     */
    public static String getMockLineKey(long userId, long paperId) {
        return Joiner.on("_").join(RedisKeyConstant.MOCK_EXAM_LINE_PREFIX, userId, paperId);
    }


    /**
     * 用户参加模考的地区信息
     *
     * @param paperId
     * @return
     */
    public static String getMockUserAreaPrefix(long paperId) {
        return Joiner.on("_").join(RedisKeyConstant.MOCK_USER_AREA_PREFIX, paperId);
    }


    /**
     * 成绩报告
     *
     * @param paperId
     * @param userId
     * @return
     */
    public static String getMockExamReportPrefix(long paperId, int userId) {
        return Joiner.on("_").join(RedisKeyConstant.MOCK_EXAM_REPORT_PREFIX, paperId, userId);
    }


    /**
     * 用户申论模考成绩zSet
     *
     * @return
     */
    public static String getEssayUserScoreKey(long paperId) {
        return Joiner.on("_").join(RedisKeyConstant.ESSAY_USER_SCORE_PREFIX, paperId);
    }


    /**
     * 申论总成绩
     *
     * @return
     */
    public static String getEssayScoreSumKey(long paperId) {
        return Joiner.on("_").join(RedisKeyConstant.ESSAY_SCORE_SUM_PREFIX, paperId);
    }

    /**
     * 用户申论模考成绩 地区zSet
     *
     * @return
     */
    public static String getEssayUserAreaScoreKey(long paperId, long areaId) {
        return Joiner.on("_").join(RedisKeyConstant.ESSAY_USER_AREA_SCORE_PREFIX, paperId, areaId);
    }


    /**
     * 用户模考总成绩zSet
     *
     * @return
     */
    public static String getMockUserTotalScoreKey(long paperId) {
        return Joiner.on("_").join(RedisKeyConstant.MOCK_USER_TOTAL_SCORE_PREFIX, paperId);
    }


    /**
     * 用户模考总成绩 地区zSet
     *
     * @return
     */
    public static String getMockUserAreaTotalScoreKey(long paperId, long areaId) {
        return Joiner.on("_").join(RedisKeyConstant.MOCK_USER_AREA_TOTAL_SCORE_PREFIX, paperId, areaId);
    }


    /**
     * 模考总成绩
     *
     * @return
     */
    public static String getMockScoreSumKey(long paperId) {
        return Joiner.on("_").join(RedisKeyConstant.MOCK_SCORE_SUM_PREFIX, paperId);
    }

    /**
     * 用户行测成绩 Zset
     *
     * @return
     */
    public static String getUserPracticeScoreKey(long paperId) {
        return Joiner.on("_").join(RedisKeyConstant.USER_PRACTICE_SCORE_PREFIX, paperId);
    }


    public static String getMockAutoSubmitLockKey() {
        return RedisKeyConstant.MOCK_AUTO_SUBMIT_LOCK;
    }


    /**
     * 用户申论大赛报名人数
     *
     * @param paperId
     * @return
     */
    public static String getTotalEnrollCountKey(long paperId) {
        return Joiner.on("_").join(RedisKeyConstant.MOCK_ENROLL_COUNT_PAPER, paperId);
    }


    /**
     * 获取试题标准答案key
     *
     * @return
     */
    public static final String getStandardAnswerKey(long detailId) {
        return Joiner.on("_").join("essay_standard_answer_V1", detailId);
    }


    /**
     * 获取试题标准答案key(多个答案)
     *
     * @return
     */
    public static final String getRefrenceAnswerKey(long detailId) {
        return Joiner.on("_").join("essay_reference_answer_new_", detailId);
    }

    /**
     * 获取试题材料key
     *
     * @param baseId 试题baseId
     * @return
     */
    public static final String getMaterialKey(long baseId) {
        return Joiner.on("_").join("essay_material", baseId);
    }


    /**
     * 行测的信息
     *
     * @return
     */
    public static String getPracticeInfoKey() {
        return "practice_info";
    }


    /**
     * 用户的模考答题卡列表
     *
     * @return
     */
    public static String getUserMockAnswerListKey(int userId) {
        return Joiner.on("_").join(USER_MOCK_ANSWER_LIST, userId);

    }


    /**
     * 获取试题类型信息key
     *
     * @return
     */
    public static final String getQuestionTypeKey(int type) {
        return Joiner.on("_").join("essay_question_type", type);
    }

    /**
     * 获取题组下试题信息
     *
     * @return
     */
    public static final String getQuestionOfGroupKey(long similarId) {
        return Joiner.on("_").join("essay_question_of_similar", similarId);
    }

    /**
     * 获取关闭订单锁的key
     *
     * @return
     */
    public static String getOrderCloseLockKey() {
        return "order_close_lock";
    }

    public static String getQuestionCorrectNumKey(Long questionBaseId) {
        return Joiner.on("_").join("question_correct_num", questionBaseId);

    }

    public static String getQuestionCorrectNumKey(Long questionBaseId, int correctMode) {
        return Joiner.on("_").join("question_correct_num", questionBaseId, correctMode);

    }


    public static String getUserQuestionCorrectNumKey(Long questionBaseId, int userId) {
        return Joiner.on("_").join("user_question_correct_num", userId, questionBaseId);

    }


    public static String getUserPaperAnswerKey(Long paperAnswerId) {
        return Joiner.on("_").join("user_paper_answer", paperAnswerId);
    }

    public static String getUserPaperAnswerDetailKey(Long paperAnswerId, int type) {
        return Joiner.on("_").join("user_paper_answer", paperAnswerId, type);
    }


    /**
     * 估分列表标题
     *
     * @return
     */
    public static String getEssayGuFenPageTitleKey() {
        return "gu_fen_page_title";
    }


    /**
     * 估分列表
     *
     * @return
     */
    public static String getEssayGuFenPaperListKey() {
        return "gu_fen_paper_list";
    }


    /**
     * 估分试卷信息
     *
     * @return
     */
    public static String getEssayGuFenPaperInfoKey(Long paperId) {

        return Joiner.on("_").join("gu_fen_paper_info", paperId);
    }

    /**
     * 试题搜索是否展示材料和题干开关
     */
    public static String getEssaySearchSwitchKey() {
        return ("search_switch");
    }

    /**
     * 获取教育后台帐号
     *
     * @return
     */
    public static String getJYUserKey() {
        return "jy_user_key_list";
    }


    // 拍照答题开关
//    public static final String PHOTO_ANSWER_KEY_ANDROID = "essay-server.photo_answer_android";
//    public static final String PHOTO_ANSWER_KEY_IOS = "essay-server.photo_answer_ios";
//    public static final String PHOTO_ANSWER_KEY_IOS_OLD = "essay-server.photo_answer_ios_old";
//    public static final String PHOTO_ANSWER_KEY_ANDROID_OLD = "essay-server.photo_answer_android_old";
//
//    // 拍照答题对接第三方
//    public static final String PHOTO_ANSWER_TYPE_IOS = "essay-server.photo_answer_type_ios";
//    public static final String PHOTO_ANSWER_TYPE_ANDROID = "essay-server.photo_answer_type_android";
//    public static final String PHOTO_ANSWER_MSG = "essay-server.photo_answer_msg";

}
