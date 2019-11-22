package com.huatu.tiku.essay.constant.label;

import com.google.common.base.Joiner;

/**
 * @author zhaoxi
 * @Description: 批注相关key
 * @date 2018/7/11上午11:02
 */
public class LabelRedisKeyConstant {
    /**
     * 获取 关闭未完成批注 的key
     * @return
     */
    public static String getLabelCloseLockKey() {
        return "unfinished_label_close_lock";
    }


    /**
     * 获取 下一篇批注 的key
     * @return
     */
    public static String getNextLabelLockKey() {
        return "get_next_label_lock";
    }


    /*
     *  白名单用户列表
     */
    public static String  getFinalLabelTeacherList() {
        return "label_final_teacher_list";
    }

    /*
     *  白名单用户列表
     */
    public static String  getVIPLabelTeacherList() {
        return "label_vip_teacher_list";
    }

    /*
     *  用户放弃数据列表(该用户当天不再抽到的题目)
     */
    public static String  getTeacherGiveUpListKey(String teacher) {

        return  Joiner.on("_").join("teacher_label_give_up_list",teacher);
    }


    /*
     *  放弃数据列表（大家都不再抽到的题目）
     */
    public static String  getGiveUpListKey() {

        return "label_give_up_list";
    }

}
