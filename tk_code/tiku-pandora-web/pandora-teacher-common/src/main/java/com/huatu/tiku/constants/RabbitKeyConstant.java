package com.huatu.tiku.constants;

/**
 * Created by huangqingpeng on 2018/8/25.
 */
public class RabbitKeyConstant {

    public final static String SyncQuestionByPaper = "sync_question_by_paper_queue";
    public final static String SyncPaperQuestion = "sync_paper_question_queue";
    public static final String NOTICE_FEEDBACK_CORRECT = "notice_push_feedback_correct";
    public static final String QUESTION_ERROR_DOWNLOAD_TASK = "question_error_download_task";
    /**
     * 同步试题到mysql
     *
     * @param env
     * @return
     */
    public static String getQuestion_2_mysql(String env) {
       System.out.println("env=:"+env);
        return "sync_question_2_mysql_" + env;
//        return "sync_question_2_mysql_test" ;
    }

    /**
     * 同步试题到mongo
     *
     * @param env
     * @return
     */
    public static String getQuestion_2_mongo(String env) {
        System.out.println("env=:"+env);
        return "sync_question_2_mongo_" + env;
//        return "sync_question_2_mongo_test";
    }

    public static String getErrorDownload(String env) {
        System.out.println("env=:"+env);
        return QUESTION_ERROR_DOWNLOAD_TASK + "_" + env;
    }
}
