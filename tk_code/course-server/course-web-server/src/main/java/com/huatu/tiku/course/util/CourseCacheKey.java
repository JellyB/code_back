package com.huatu.tiku.course.util;

/**
 * 直接使用的统一用Public,方法中使用的再private定义后使用，方便日后统一建立后台缓存更新系统
 * @author hanchao
 * @date 2017/9/13 10:50
 */
public class CourseCacheKey {
    /**
     * 缓存数据增加标记使用等
     */
    public static final String CACHE_FLAG = "_cache";
    public static final String TIMESTAMP_FLAG = "_timestamp";


    /**
     * 课程搜索配置条件
     */
    public static final String RECORDING_SETTINGS = "recording_settings";
    public static final String LIVE_SETTINGS = "live_settings";
    public static final String LEVEL_SETTINGS = "level_settings";

    /**
     * 下单失败的队列
     */
    public static final String ORDERS_QUEUE = "orders_queue";


    /**
     * 课程详情
     */
    private static final String COURSE_DETAIL_V2 = "course_detail$%s";
    private static final String COURSE_DETAIL_V3 = "course_detail_v3$%s";
    private static final String COURSE_ANALYSIS_V6 = "course_analysis_v6$%s";
    private static final String COURSE_MINE_V6 = "course_mine_v6$%s";
    /**
     * 直播列表
     */
    private static final String COURSE_LIST_V2 = "live_list$%s";
    private static final String COURSE_LIST_V3 = "live_list_v3$%s";

    private static final String REWARD_RECORD = "reward_record$%s$%s";
    /**
     * 日历详情
     */
    private static final String CALENDAR_DETAIL_V6 = "calendar_detail_v6$%s";
    private static final String CALENDAR_LEARN_V6 = "calendar_learn_v6$%s";


    /**
     * 直播录播数据上报key
     */
    private static final String PROCESS_REPORT_DELAY_QUEUE = "process_report_delay";

    private static final String PROCESS_LOG_SYLLABUS_INFO = "process_log_syllabus_v6$%s";
    private static final String ESSAY_COURSE_WORK_SYLLABUS_INFO = "essay_course_work_syllabus_v7$$s_%s_%s";

    private static final String PROCESS_LOG_SYLLABUS_DEAL_LIST = "process_log_syllabus_list_v6";
    private static final String USER_ACCOUNT_INFO_KEY = "user_account_info_key";

    /**
     * 课后作业统计key
     */
    private static final String COURSE_WORK_DEAL_DATA = "course_work_deal_v1$%s$%s";
    private static final String COURSE_WORK_RANK_INFO = "course_work_rank_info_v1$%s$%s";
    private static final String COURSE_LIVE_BACK_LOG_INFO = "course_live_back_log_info_v1$%s$%s";
    /**
     * 申论课后作业 key
     */
    private static final String COURSE_WORK_ESSAY_ISALERT = "course_work_essay_isAlert_v1%s";
    /**
     * 已经处理完后的课后作业 redis key
     */
    public static final String COURSE_WORK_REPORT_USERS_ALREADY_PROCESSED = "course_work_report_users_already_processed";


    /**
     * IOS 内侧版本信息
     */
    public static final String IOS_AUDIT_VERSION = "ios_audit_versions";

    public static String courseDetailV2(Integer courseId){
        return String.format(COURSE_DETAIL_V2,courseId);
    }

    public static String courseDetailV3(Integer courseId){
        return String.format(COURSE_DETAIL_V3,courseId);
    }

    public static String courseAnalysisV6(String classId){ return String.format(COURSE_ANALYSIS_V6, classId); }

    public static String courseListV2(String sign){
        return String.format(COURSE_LIST_V2,sign);
    }

    public static String courseListV3(String sign){
        return String.format(COURSE_LIST_V3,sign);
    }

    public static String calendarDetailV6(String sign){
        return String.format(CALENDAR_DETAIL_V6,sign);
    }

    public static String calendarLearnV6(String sign){
        return String.format(CALENDAR_LEARN_V6, sign);
    }

    public static String courseMineV6(String sign){
        return String.format(COURSE_MINE_V6, sign);
    }

    public static String rewardRecord(String action,int uid){
        return String.format(REWARD_RECORD,action,uid);
    }

    public static String getProcessReportDelayQueue(){
        return PROCESS_REPORT_DELAY_QUEUE;
    }

    /**
     * 获取 syllabusInfo cache key
     * @param syllabusId
     * @return
     */
    public static String getProcessLogSyllabusInfo(Long syllabusId){return String.format(PROCESS_LOG_SYLLABUS_INFO, syllabusId);}

    public static String getUserAccountInfoKey(){
        return USER_ACCOUNT_INFO_KEY;
    }

    @Deprecated
    public static String getProcessLogSyllabusDealList(){return PROCESS_LOG_SYLLABUS_DEAL_LIST;}

    /**
     * 用户提交答题卡userId set 缓存
     * @param courseType
     * @param courserId
     * @return
     */
    public static String getCourseWorkDealData(Integer courseType, Long courserId){return String.format(COURSE_WORK_DEAL_DATA, courseType, courserId);}

    /**
     * 课后作业统计排名信息
     * @param courseType
     * @param courseId
     * @return
     */
    public static String getCourseWorkRankInfo(Integer courseType, Long courseId){return String.format(COURSE_WORK_RANK_INFO, courseType, courseId);}

    /**
     * 查询直播回放的直播课件id
     * @param roomId
     * @param liveBackWareId
     * @return
     */
    public static String findByRoomIdAndLiveCourseWareId(long roomId, long liveBackWareId){
        return String.format(COURSE_LIVE_BACK_LOG_INFO, roomId, liveBackWareId);
    }

    /**
     * 获取申论课后作业未读数
     * @param userId
     * key userId
     * values syllabusIds
     * @return
     */
    public static String getCourseWorkEssayIsAlert(long userId){
        return String.format(COURSE_WORK_ESSAY_ISALERT, userId);
    }

    /**
     * 申论课后作业大纲信息
     * @param courseType
     * @param syllabusId
     * @param answerCardId
     * @return
     */
    public static String getEssayCourseWorkSyllabusInfo(Integer courseType, Long syllabusId, Long answerCardId){
        return String.format(ESSAY_COURSE_WORK_SYLLABUS_INFO, courseType, syllabusId, answerCardId);
    }

    public static String getBaseCourseInfoKey(Integer classId) {
        return String.format("estimate_course_base_info_%s",classId);
    }
}
