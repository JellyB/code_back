package com.huatu.tiku.interview.util.json;

import com.google.gson.Gson;

/**
 * @Author ZhenYang
 * @Date Created in 2018/1/12 16:45
 * @Description
 */
public class JsonUtil {

    public static String toJson(Object object) {
        Gson gson = new Gson();
        String result = gson.toJson(object);
        gson = null;
        return result;
    }


    public static <T> T fromJson(String json, Class<T> classOfT){
        Gson gson = new Gson();
        T t = gson.fromJson(json, classOfT);
        gson = null;//
        return t;
    }
}