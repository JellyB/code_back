package com.huatu.tiku.essay.util.video;

import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.TreeMap;

/**
 * 百家云相关工具类
 */
@Service
public class YunUtil {

    public static final String BJY_PATH = "http://api.baijiacloud.com/m/video/player";
    @Value("${bjy_partner_id}")
    private String bjyPartnerId;
    @Value("${bjy_partner_key}")
    private String bjyPartnerKey;
    public TreeMap<String, Object> getParamTree(){
        TreeMap<String, Object> treeMap = Maps.newTreeMap();
        treeMap.put("partner_id", bjyPartnerId);
        treeMap.put("timestamp", System.currentTimeMillis());
        return treeMap;
    }

    public String parseParams(TreeMap<String, Object> params) {
        // 拼接已有参数
        StringBuilder result = new StringBuilder();
        params.entrySet().forEach(param -> {
            result.append(param.getKey()).append("=").append(param.getValue()).append("&");
        });
        // 获取sign
        String signParams = result.toString() + "partner_key="+bjyPartnerKey;

        String sign = Hashing.md5().hashString(signParams, Charset.forName("UTF-8")).toString().toLowerCase();
        return result.append("&sign").append("=").append(sign).toString();
    }


    public static String getVideoUrl(Integer yunVideoId, String token){
        StringBuilder sb = new StringBuilder();
        sb.append(BJY_PATH)
                .append("?vid=")
                .append(yunVideoId)
                .append("&token=")
                .append(token);
        return sb.toString();
    }

}
