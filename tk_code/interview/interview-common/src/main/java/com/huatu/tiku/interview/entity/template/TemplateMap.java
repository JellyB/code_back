package com.huatu.tiku.interview.entity.template;

import lombok.Data;

import java.util.TreeMap;

/**
 * @Author ZhenYang
 * @Date Created in 2018/1/13 17:35
 * @Description
 */
@Data

public class TemplateMap {
    private String key;
    private TreeMap<String, String> data;

    public TemplateMap(String key, TreeMap<String, String> data) {
        this.key = key;
        this.data = data;
    }
}
