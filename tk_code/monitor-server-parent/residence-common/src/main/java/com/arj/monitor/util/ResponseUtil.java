package com.arj.monitor.util;

import com.arj.monitor.exception.ErrorResult;
import com.arj.monitor.exception.SuccessMessage;


/**
 * @author zhouwei
 * @Description: 工具类
 * @create 2018-12-13 16:29:50
 **/
public class ResponseUtil {


    /**
     * 是否失败的响应
     * @param response
     * @return
     */
    public static boolean isFailure(ErrorResult response){
        if(response == null || response.getCode() != SuccessMessage.SUCCESS_CODE){
            return true;
        }
        return false;
    }


}
