package com.huatu.tiku.interview.util.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @Author ZhenYang
 * @Date Created in 2018/1/12 19:09
 * @Description
 */
public class MyGsonBuilder {
    public static final GsonBuilder INSTANCE = new GsonBuilder();

    static {
        INSTANCE.disableHtmlEscaping();


    }

    public static Gson create() {
        return INSTANCE.create();
    }

}
