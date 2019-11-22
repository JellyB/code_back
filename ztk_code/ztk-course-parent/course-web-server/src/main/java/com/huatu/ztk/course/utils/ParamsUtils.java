package com.huatu.ztk.course.utils;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.huatu.ztk.utils.encrypt.SignUtil;
import org.apache.commons.collections.MapUtils;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by linkang on 11/30/16.
 */
public class ParamsUtils {

    /**
     * 组装请求参数
     * @param parameterMap
     * @return
     */
    public static String makeParams(Map<String, Object> parameterMap) {
        if (MapUtils.isEmpty(parameterMap)) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        parameterMap.keySet().stream()
                .filter(key->parameterMap.get(key) != null)
                .forEach(key -> stringBuilder.append(key + "=" + parameterMap.get(key) + "&"));
        //去掉最后的&
        return stringBuilder.substring(0, stringBuilder.length() - 1);
    }


    /**
     * 组装json形式的参数
     * @param parameterMap
     * @return
     */
    public static String makeJsonParams(Map<String, Object> parameterMap) {
        if (MapUtils.isEmpty(parameterMap)) {
            return "";
        }
        JsonObject jsonObject = new JsonObject();
        parameterMap.keySet().stream()
                .filter(key->parameterMap.get(key) != null)
                .forEach(key -> jsonObject.addProperty(key, parameterMap.get(key).toString()));
        return jsonObject.toString();
    }

    /**
     * 参数排序md5后作为redis key
     * @param params
     * @return
     */
    public static String getSign(Map<String,Object> params){
        TreeMap treeMap = Maps.newTreeMap();
        treeMap.putAll(params);
        return SignUtil.getPaySign(treeMap,null);
    }
}
