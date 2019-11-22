package com.huatu.ztk.commons;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * 考试科目列表
 * 这个主要是针对知识点的分类
 * Created by shaojieyue
 * Created time 2016-05-24 10:47
 */
public class SubjectType {
    /**
     * 公务员-行测
     */
    public static final int GWY_XINGCE = 1;
    /**
     * 公务员-行测+申论
     */
    public static final int GWY_XINGCE_SHENLUN = 14;

    /**
     * 事业单位-公共基础
     */
    public static final int SYDW_GONGJI = 2;

    /**
     * 事业单位-行测
     */
    public static final int SYDW_XINGCE = 4;
    /**
     * 金融-中国银行
     */
    public static final Set<Integer> YHZP_SUBJECTS = Sets.newHashSet();
    static{
        YHZP_SUBJECTS.add(100100126);
        YHZP_SUBJECTS.add(100100127);
        YHZP_SUBJECTS.add(100100128);
        YHZP_SUBJECTS.add(100100129);
        YHZP_SUBJECTS.add(100100130);
        YHZP_SUBJECTS.add(100100165);
    }
}
