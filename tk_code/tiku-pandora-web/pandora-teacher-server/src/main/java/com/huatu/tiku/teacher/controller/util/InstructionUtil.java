package com.huatu.tiku.teacher.controller.util;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Created by huangqingpeng on 2019/3/21.
 */
public class InstructionUtil {

    private static Map matchDesc = Maps.newHashMap();
    private static Map smallEstimateDesc = Maps.newHashMap();
    private static final String DESC_KEY = "desc";
    static {
        matchDesc.put(DESC_KEY,"考试说明\n" +
                "1. 开考前5分钟可提前进入考场查看题目，开考30分钟后则无法报名和进入考试 。考生可提前下载试卷进行打印答题，在考试界面有下载的按钮。\n" +
                "2. 开始答题后不可暂停计时，如需完全退出可直接提交试卷;考试结束自动交卷。\n" +
                "3. 分享“报名成功”截图至微博并@华图在线官微，获得更多惊喜。");
        smallEstimateDesc.put(DESC_KEY,"模考说明：\n" +
                "1.本模考只有在规定时间内可以进行；\n" +
                "2.开始答题后不可暂停计时，交卷之后方可查看解析；\n" +
                "3.中途不可以退出，退出页面会自动提交答题卡。");
    }
    public static Map getMatchDesc() {
        return matchDesc;
    }

    public static Map getSmallEstimateDesc() {
        return smallEstimateDesc;
    }
}
