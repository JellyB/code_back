package com.huatu.tiku.essay.constant.match;


import com.google.common.base.Joiner;

/**
 * @author zhouwei
 * @Description: 模考相关缓存key
 * @create 2017-12-16 下午2:15
 **/
public class MatchRedisKeyConstant {


    /**
     * 模考大赛降级开关
     * @return
     */
    public static final String getMockDegradeSwitch(){
        return  "essay_mock_degrade_switch";
    }


    /**
     * 模考大赛降级列表页信息缓存
     * @return
     */
    public static final String getMockDegradeList(){
        return  "essay_mock_degrade_list";
    }


    /**
     * 获取题组下试题信息
     * @return
     */
    public static final String getQuestionOfGroupKey(long similarId){
        return  Joiner.on("_").join("essay_question_of_similar",similarId);
    }

    /**
     * 获取关闭订单锁的key
     * @return
     */
    public static String getOrderCloseLockKey() {
        return "order_close_lock";
    }


    /**
     * 获取当前可用模考
     */
    public static String getCurrentMatchKey(){
        return "essay_current_match";
    }

    /**
     * 获取解析课信息
     */
    public static String getMockCourseInfo(String courseIds){
        return  Joiner.on("_").join("essay_mock_course",courseIds);
    }


    /**
     * 往期模考
     */
    public static String getPastMockKey(int page,int tag){
        return Joiner.on("_").join("essay_past_mock",page,tag);
    }

}
