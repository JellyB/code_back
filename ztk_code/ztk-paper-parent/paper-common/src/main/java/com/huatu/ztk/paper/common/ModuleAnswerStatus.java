package com.huatu.ztk.paper.common;

/**
 * @author zhaoxi
 * @Description: 各个模块作答完成情况（暂时只有招警机考需要）
 * @date 2018/8/22下午1:15
 */
public class ModuleAnswerStatus {
    /**
     * 初始化（未开始）
     */
    public static final int INIT = 0;

    /**
     * 进行中
     */
    public static final int ANSWERING = 1;

    /**
     * 已完成
     */
    public static final int FINISHED = 2;

}
