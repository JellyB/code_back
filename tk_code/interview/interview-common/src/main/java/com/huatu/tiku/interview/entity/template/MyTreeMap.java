package com.huatu.tiku.interview.entity.template;

import java.util.TreeMap;

/**
 * @Author ZhenYang
 * @Date Created in 2018/1/13 17:30
 * @Description
 */
public class MyTreeMap{

    public static TreeMap createMap(TemplateMap... tms){
        TreeMap TreeMap = new TreeMap();
        for (TemplateMap tm : tms){
            TreeMap.put(tm.getKey(),tm.getData());
        }
        return TreeMap;
    }
}
