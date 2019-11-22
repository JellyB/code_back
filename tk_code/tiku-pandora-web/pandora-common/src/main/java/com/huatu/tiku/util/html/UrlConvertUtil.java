package com.huatu.tiku.util.html;

import com.google.common.collect.Maps;
import com.huatu.tiku.baseEnum.UrlConvertEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * 网址url通过java访问时，需要对特殊字符做转换，才能保证访问地址的正确性
 * Created by huangqingpeng on 2018/9/27.
 */
public class UrlConvertUtil {
    /**
     * 特殊字符转换对应逻辑
     */
    public static final Map<String, String> special_char_convert_map = Maps.newHashMap();

    static {
        for (UrlConvertEnum urlConvertEnum : UrlConvertEnum.values()) {
            if (urlConvertEnum.getFlag() == 1) {
                special_char_convert_map.put(urlConvertEnum.getPreKey(), urlConvertEnum.getValue());
            }
        }
    }

    /**
     * 网址特殊字符转换
     * @param url
     * @return
     */
    public static String convert(String url){
        if(StringUtils.isBlank(url)){
            return url;
        }
        for(Map.Entry<String,String> entry :special_char_convert_map.entrySet()){
            url = url.replace(entry.getKey(),entry.getValue());
        }
        return url;
    }
}
