package com.huatu.tiku.match.common;

import com.google.common.collect.Maps;
import lombok.*;

import java.util.Map;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2018-10-25 上午10:29
 **/
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag{
    /**
     * tag id
     *
     */
    private int id;

    /**
     * tag name
     */
    private String name;

    /**
     * tag channel
     * 1 非申论
     * 2 申论
     */
    private int flag;

    /**
     * subject
     */
    private int subject;

    /**
     * category
     */
    private int category;

    public Map<String,Object> toMap(){
        Map<String,Object> map = Maps.newHashMap();
        map.put("key", getId());
        map.put("value", getName());
        return map;
    }
}