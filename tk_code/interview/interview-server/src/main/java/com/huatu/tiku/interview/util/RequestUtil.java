package com.huatu.tiku.interview.util;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;

import java.util.Map;

/**
 * @author hanchao
 * @date 2017/8/29 19:10
 */
@Slf4j
public class RequestUtil {

    /**
     * 组装请求参数
     * @param parameterMap
     * @return
     */
    public static String expandUrl(Map<String, Object> parameterMap) {
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
     * 对参数进行url展开后加密
     * @param params
     * @return
     */
    public static Map<String,Object> encryptParams(Map<String,Object> params){
        Map<String,Object> result = Maps.newHashMapWithExpectedSize(1);
        result.put("p",encrypt(params));
        return result;
    }

    public static String encrypt(Map<String,Object> params){
        String paramsUrl = expandUrl(params);
        //加密
        String encryptparams = Crypt3Des.encryptMode(paramsUrl);
        return encryptparams;
    }


    /**
     * 对参数进行json序列化后加密
     * @param params
     * @return
     */
    public static Map<String,Object> encryptJsonParams(Map<String, Object> params){
        Map<String,Object> result = Maps.newHashMapWithExpectedSize(1);
        if (MapUtils.isEmpty(params)) {
            return result;
        }
        result.put("p",encryptJson(params));
        return result;
    }

    public static String encryptJson(Map<String,Object> params){
        JSONObject jsonObject = new JSONObject();
        params.keySet().stream()
                .filter(key->params.get(key) != null)
                .forEach(key -> jsonObject.put(key, params.get(key).toString()));
        //去掉最后的&
        String paramsUrl = jsonObject.toString();
        //加密
        String encryptparams = Crypt3Des.encryptMode(paramsUrl);
        return encryptparams;
    }

}
