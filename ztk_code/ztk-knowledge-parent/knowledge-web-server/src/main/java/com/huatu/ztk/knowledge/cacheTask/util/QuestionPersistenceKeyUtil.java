package com.huatu.ztk.knowledge.cacheTask.util;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;

/**
 * Created by junli on 2018/4/12.
 */
public class QuestionPersistenceKeyUtil {

    /**
     * 修改 key 失效时间
     */
    public static final int TTL = 90;

    /**
     * 把 cacheKey $type_$uid_1_$pointId 拆分
     *
     * @param cacheKey
     * @return
     */
    public final static HashMap<String, String> cacheKeyToMap(String cacheKey) {
        if (StringUtils.isEmpty(cacheKey)) {
            return new HashMap<>();
        }
        String[] split = cacheKey.split("_");
        if (split.length < 4) {
            return new HashMap<>();
        }
        return new HashMap<String, String>() {{
            put("userId", split[1]);
            put("pointId", split[3]);
        }};
    }
}
