package com.huatu.tiku.course.util;

import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;

import java.util.Map;

import static com.huatu.common.Result.SUCCESS_CODE;

/**
 * 砖题库的http 请求的数据解析
 * 正常情况，如果在远端出现异常，会直接进入fail回调，进入该部分的逻辑都是正常的响应
 * Created by lijun on 2018/6/20
 */
public class ZTKResponseUtil {

    private static final int DEFAULT_CODE = 50000010;

    /**
     * 简单的判断 http 请求的数据是否合法
     *
     * @param data 请求返回中的 data 部分
     * @return
     */
    public static Object build(Object data) {
        if (!isIllegal(data)) {
            return null;
        }
        Map<String, Object> map = (Map<String, Object>) data;
        if (!isSuccessCode(map)) {
            int code = Integer.valueOf(map.get("code").toString());
            String message = map.get("message") != null ? String.valueOf(map.get("message")) : "服务器内部错误！";
            throw new BizException(ErrorResult.create(code, message));
        }
        return map.get("data");
    }

    /**
     * 判断返回的数据是否合法
     *
     * @param data
     * @return
     */
    private static boolean isIllegal(Object data) {
        if (null == data || !(data instanceof Map)) {//数据不为空 且 是map
            return false;
        }
        if (null == ((Map) data).get("code") || null == ((Map) data).get("data")) {
            //返回的结果集中 必须有 code 和 data
            return false;
        }
        return true;
    }

    /**
     * 判断是否为正确响应
     *
     * @param data
     * @return
     */
    private static boolean isSuccessCode(Map<String, Object> data) {
        int code = Integer.valueOf(String.valueOf(data.get("code")));
        return code == SUCCESS_CODE;
    }

    public static Object defaultResult(){
        throw new BizException(ErrorResult.create(DEFAULT_CODE, "远程调用失败"));
    }
}
