package com.huatu.ztk.knowledge.constant;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by huangqp on 2017\12\4 0004.
 */
public class SubjectConstant {
    /**
     * 金融-中国银行
     */
    public static final Set<Integer> YHZP_SUBJECTS = new HashSet<>();
    static{
        YHZP_SUBJECTS.add(100100126);   //中国银行
        YHZP_SUBJECTS.add(100100127);   //中国工商银行
        YHZP_SUBJECTS.add(100100128);   //中国农业银行
        YHZP_SUBJECTS.add(100100129);   //中国建设银行
        YHZP_SUBJECTS.add(100100130);   //交通银行
        YHZP_SUBJECTS.add(100100165);   //考点题库
    }
}
