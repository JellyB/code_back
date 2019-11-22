package com.huatu.tiku.essay.constant.status;

/**
 * Created by x6 on 2017/12/5.
 */
public class SystemConstant {


    //生成试卷&试题文件
    public final static String CREATE_PDF_ROUTING_KEY = "create_essay_pdf_queue";
    //生成试卷&试题答题卡
    public final static String CREATE_ESSAY_MOCK_ANSWER_CARD_QUEUE = "zxtest_create_essay_mock_answer_card_queue";
    //处理关键句分词的队列名
    public final static String ESSAY_STANDARD_ANSWER_KEY_PHRASE_QUEUE = "essay_standard_answer_key_phrase_queue";
    //批改完成
    public final static String ANSWER_CORRECT_FINISH_QUEUE = "zxtest_mock_answer_correct_finish_queue";
    //试题批改队列名称
    public final static String MOCK_ANSWER_CORRECT_ROUTING_KEY = "mock_answer_correct_queue_essay";
    //生成试卷批改报告
    public final static String ESSAY_PAPER_REPORT_QUEUE = "essay_paper_report_queue";
    //人工批改完成生成评语标签和阅卷评论
    public final static String ESSAY_MANUAL_CORRECT_FINISH_QUEUE = "essay_manual_correct_finish_queue";


    //APP_ID
    public final static String APP_ID = "wxd0611111b31aa452";
    //商户ID
    public final static String MCH_ID = "1310649601";
    //支付ID
    public final static String PARTNER_KEY = "af925cea7762df427b0148e43c7c1c8a";
    //AppSecret
    public final static String APP_SECRET = "fee78ac9f3e5fc2d452affb548c88d2d";
    //订单创建的ip
    public final static String SPBILL_CREATE_IP = "192.168.100.20";
    //直播转回放申论处理队列
    public final static String CALL_BACK_FAN_OUT_ESSAY = "call_back_queue_essay";
    //直播转回放行测处理队列
    public final static String CALL_BACK_FAN_OUT_CIVIL = "call_back_queue_civil";
    //直播转回放 fan out exchange
    public final static String CALL_BACK_FAN_OUT = "call_back_fan_out";

}
