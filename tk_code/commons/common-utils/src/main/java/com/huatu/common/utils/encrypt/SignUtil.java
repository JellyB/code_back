package com.huatu.common.utils.encrypt;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author hanchao
 * @date 2017/2/12 17:43
 */
@Slf4j
public class SignUtil {
    /**
     * 默认的MD5,pay sign,微信可用
     *
     * @param params
     * @return
     */
    public static String getPaySign(Map<String, Object> params, String key) {
        if (key != null) {
            key = "&key=" + key;
        }
        return getSign(params, Sets.newHashSet("sign", "key"), key).toUpperCase();
    }

    /**
     * 默认是utf-8获取bytes
     * @param params
     * @param ignoreKeys
     * @param key
     * @return
     */
    public static String getSign(Map<String, ?> params, Set<String> ignoreKeys, String key) {
        Preconditions.checkNotNull(params);
        TreeMap<String, Object> treeMap = null;
        if (params instanceof TreeMap) {
            treeMap = (TreeMap<String, Object>) params;
        } else {
            treeMap = Maps.newTreeMap();
            treeMap.putAll(params);
        }
        StringBuilder str = new StringBuilder();
        for (Iterator<Map.Entry<String, Object>> it = treeMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Object> entry = it.next();
            if (StringUtils.isNotBlank(entry.getKey()) && entry.getValue() != null && StringUtils.isNotBlank(entry.getValue().toString())) {
                if (ignoreKeys.contains(entry.getKey())) {
                    continue;
                }
                str.append(entry.getKey());
                str.append("=");
                str.append(entry.getValue());
                str.append("&");
            }
        }
        if (str.toString().endsWith("&")) {
            str = new StringBuilder(str.substring(0, str.length() - 1));
        }
        if (key != null) {
            str.append(key);
        }
        if (log.isDebugEnabled()) {
            log.debug("pay sign params : {}", params.toString());
            log.debug("pay sign str : {}", str);
        }
        return DigestUtils.md5Hex(str.toString());
    }

}
