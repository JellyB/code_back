package com.huatu.tiku.interview.util.po;

import com.huatu.tiku.interview.constant.ResultEnum;
import com.huatu.tiku.interview.exception.ReqException;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * @Author ZhenYang
 * @Date Created in 2018/1/25 11:08
 * @Description
 */
public class ClussStringUtil {

    public static String[] getList(String classIds){
        String[] split = StringUtils.split(classIds, ",");
        try {
            for (String s : split){
                Long.valueOf(s);
            }
        }catch (Exception e){
            throw new ReqException(ResultEnum.CLASS_CAST_ERROR);
        }
        return split;
    }
}
