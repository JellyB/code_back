package com.huatu.tiku.essay.constant.status;

import java.util.HashMap;
import java.util.Map;

public class PdfNumberMap {
    static final Map<Integer, String> NUMBER_MAP;

    static {
        NUMBER_MAP = new HashMap<>();
        NUMBER_MAP.put(1, "一");
        NUMBER_MAP.put(2, "二");
        NUMBER_MAP.put(3, "三");
        NUMBER_MAP.put(4, "四");
        NUMBER_MAP.put(5, "五");
        NUMBER_MAP.put(6, "六");
        NUMBER_MAP.put(7, "七");
        NUMBER_MAP.put(8, "八");
        NUMBER_MAP.put(9, "九");
    }

    /**
     * 通过中文number获取对应的大写中国汉字
     * @param numbert
     * @return
     */
    public static String getPdfNumberMap(Integer numbert) {
        return NUMBER_MAP.get(numbert);
    }

}
