package com.huatu.tiku.course.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.ErrorResult;
import com.huatu.common.Result;
import com.huatu.common.SuccessMessage;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.common.NetSchoolConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cglib.beans.ImmutableBean;

import java.util.HashMap;
import java.util.Map;

/**
 * transform
 * @author hanchao
 * @date 2017/8/29 12:28
 */
public class ResponseUtil {

    public static ErrorResult ERROR_NULL_RESPONSE = ErrorResult.create(Result.SUCCESS_CODE,"数据为空");

    public static Map<String,Object> MOCK_PAGE_RESPONSE = ImmutableMap.<String,Object>builder().put("result", Lists.newArrayList())
            .put("next",0).build();

    public static ErrorResult ERROR_PAGE_RESPONSE = ErrorResult.create(Result.SUCCESS_CODE,"数据为空",MOCK_PAGE_RESPONSE);

    public static final NetSchoolResponse DEFAULT_PAGE_EMPTY = (NetSchoolResponse) ImmutableBean.create(new NetSchoolResponse(-1000000,"服务器繁忙",MOCK_PAGE_RESPONSE));

    public static Object build(NetSchoolResponse response) {
        return build(response,false);
    }

    public static Map<String, Object> DEFAULT_PHP_PAGE =  ImmutableMap.<String, Object>builder()
            .put("from", 1)
            .put("to", 10)
            .put("per_page", 10)
            .put("data", Lists.newArrayList())
            .put("total", 0)
            .put("next_page_url", "")
            .put("current_page", 1)
            .put("last_page", 1)
            .put("path", "")
            .put("prev_page_url", "")
            .build();


    public static Map<String,Object> DEFAULT_PHP_SIMPLE_PAGE = ImmutableMap.<String, Object>builder()
                    .put("data", Lists.newArrayList())
                    .put("current_page", 1)
                    .put("last_page", 1)
                    .put("next", 0)
                    .put("total", 0)
                    .build();

    /**
     * php 请求默认 page response 对象
     */
    public static final NetSchoolResponse DEFAULT_PHP_PAGE_RESPONSE = (NetSchoolResponse)ImmutableBean.create(new NetSchoolResponse(Result.SUCCESS_CODE, "", DEFAULT_PHP_PAGE));

    /**
     * php 请求默认 simple page response 对象
     */
    public static final NetSchoolResponse DEFAULT_PHP_SIMPLE_PAGE_RESPONSE = (NetSchoolResponse) ImmutableBean.create(new NetSchoolResponse(Result.SUCCESS_CODE, "", DEFAULT_PHP_SIMPLE_PAGE));

    /**
     * 查看日志不存在非json的响应，碰到再进行调整
     * @param netSchoolResponse
     * @return
     */
    public static Object build (NetSchoolResponse netSchoolResponse,boolean needDecrypt) {
        Object data = null;
        if (isFailure(netSchoolResponse)) {
            final ErrorResult errorResult = ErrorResult.create(netSchoolResponse.getCode(), netSchoolResponse.getMsg(),netSchoolResponse.getData());
            throw new BizException(errorResult);
        } else {
            //只取data部分
            data = netSchoolResponse.getData();

            //code为1，但是没有data或者data为空字符串
            if (data == null || (data != null && StringUtils.isBlank(data.toString()))) {
                if (StringUtils.isEmpty(netSchoolResponse.getMsg())) {
                    return SuccessMessage.create();
                }
                return SuccessMessage.create(netSchoolResponse.getMsg());
            }

            //需要解密的
            if (needDecrypt) {
                String json = Crypt3Des.decryptMode(String.valueOf(data));
                data = JSON.parse(json);
            }
        }
        return data;
    }

    /**
     * 需要解密到对应class的，一般都是需要重新组装的（并行请求）
     * @param netSchoolResponse
     * @param clazz
     * @param needDecrypt
     * @param <T>
     * @return
     */
    public static <T> T build(NetSchoolResponse netSchoolResponse,Class<T> clazz,boolean needDecrypt){
        if(isFailure(netSchoolResponse) || netSchoolResponse.getData() == null){
            return null;
        }
        Object data = netSchoolResponse.getData();
        String serialize = "";
        if(needDecrypt){
            serialize = Crypt3Des.decryptMode(String.valueOf(data));
        }else{
            serialize = JSON.toJSONString(data);
        }
        return JSON.parseObject(serialize,clazz);
    }

    /**
     * 需要解密到对应class的，一般都是需要重新组装的（并行请求）
     * 泛型
     * @param netSchoolResponse
     * @param type
     * @param needDecrypt
     * @param <T>
     * @return
     */
    public static <T> T build(NetSchoolResponse netSchoolResponse, TypeReference<T> type, boolean needDecrypt){
        if(isFailure(netSchoolResponse) || netSchoolResponse.getData() == null){
            return null;
        }
        Object data = netSchoolResponse.getData();
        if(needDecrypt){
            return JSON.parseObject(Crypt3Des.decryptMode(String.valueOf(data)),type);
        }else{
            return JSON.parseObject(JSON.toJSONString(data),type);
        }
    }


    /**
     * 是否失败的响应
     * @param response
     * @return
     */
    public static boolean isFailure(NetSchoolResponse response){
        if(response == null || response.getCode() < NetSchoolConfig.SUCCESS_CODE){
            return true;
        }
        return false;
    }

    /**
     * 0是介于之间的
     * @param response
     * @return
     */
    public static boolean isSuccess(NetSchoolResponse response){
        if(response != null && response.getCode() > NetSchoolConfig.SUCCESS_CODE){
            return true;
        }
        return false;
    }

    /**
     * 严禁的成功校验
     * @param response
     * @return
     */
    public static boolean isHardSuccess(NetSchoolResponse response){
        if(response != null && response.getCode() ==  NetSchoolConfig.HARD_SUCCESS_CODE){
            return true;
        }
        return false;
    }
}
