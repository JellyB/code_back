package com.huatu.ztk.question.common;

import java.util.HashMap;
import java.util.Map;

/**
 * 试题类型
 * Created by shaojieyue
 * Created time 2016-04-18 21:55
 */
public class QuestionType {

    /**
     * 单选题
     */
    public static final int SINGLE_CHOICE =99;

    /**
     * 不定项选择
     */
    public static final int SINGLE_OR_MULTIPLE_CHOICE =101;

    /**
     * 多选题
     */
    public static final int MULTIPLE_CHOICE =100;

    /**
     * 对错题
     */
    public static final int WRONG_RIGHT=109;

    /**
     * 复合题
     */
    public static final int COMPOSITED =105;

    /**
     * 单一主观题
     */
    public static final int SINGLE_SUBJECTIVE = 106;

    /**
     * 复合主观题
     */
    public static final int MULTI_SUBJECTIVE = 107;

    public static final Map<Integer,String> types = new HashMap();

    static {
        types.put(SINGLE_CHOICE,"单选题");
        types.put(SINGLE_OR_MULTIPLE_CHOICE,"不定项选择");
        types.put(MULTIPLE_CHOICE,"多选题");
        types.put(WRONG_RIGHT,"对错题");
        types.put(COMPOSITED,"复合题");
        types.put(SINGLE_SUBJECTIVE,"单一主观题");
        types.put(MULTI_SUBJECTIVE,"复合主观题");
    }

    /**
     * 根据试题类型查询试题类型名字
     * @param type
     * @return
     */
    public static final String getName(int type){
        return types.get(type);
    }
}
