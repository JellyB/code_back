package com.huatu.ztk.monitor.common;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by linkang on 1/10/17.
 */
public class MonitorConstants {
    /**
     * 手机号列表
     */
    public static final ArrayList<String> PHONE_LIST = new ArrayList<>();


    /**
     * 每一条警告/错误的保存的时间：分钟
     */
    public static final int SAVE_INTERVAL = 60;

    /**
     * ERROR计数
     */
    public static final int MAX_ERROR_COUNT = 5;

    /**
     * 查询过去多少分钟的计数
     */
    public static final int MAX_PAST_MIN = 30;

    static {
        final String[] phones = {"18310996790"};
        Arrays.stream(phones).forEach(p -> PHONE_LIST.add(p));
    }
}
