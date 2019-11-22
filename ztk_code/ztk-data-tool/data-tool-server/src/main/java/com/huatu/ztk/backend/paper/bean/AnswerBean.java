package com.huatu.ztk.backend.paper.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ht on 2017/3/2.
 */
public class AnswerBean {

    public static final Map<Integer, String> answerMap = new HashMap();

    public static final Map<Integer,String> moduleOrderMap=new HashMap();

    static {
        answerMap.put(1, "A");
        answerMap.put(2, "B");
        answerMap.put(3, "C");
        answerMap.put(4, "D");
        answerMap.put(5, "E");
        answerMap.put(6, "F");
        answerMap.put(7, "G");
        answerMap.put(8, "H");

        moduleOrderMap.put(1,"一");
        moduleOrderMap.put(2,"二");
        moduleOrderMap.put(3,"三");
        moduleOrderMap.put(4,"四");
        moduleOrderMap.put(5,"五");
        moduleOrderMap.put(6,"六");
        moduleOrderMap.put(7,"七");
        moduleOrderMap.put(8,"八");
        moduleOrderMap.put(9,"九");
        moduleOrderMap.put(10,"十");
        moduleOrderMap.put(11,"十一");
        moduleOrderMap.put(12,"十二");
    }


}
