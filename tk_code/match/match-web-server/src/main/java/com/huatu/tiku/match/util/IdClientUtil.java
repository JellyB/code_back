package com.huatu.tiku.match.util;

import java.util.UUID;

/**
 * Created by huangqingpeng on 2019/1/11.
 */
public class IdClientUtil {

    /**
     * 生成随机的id
     *
     * @return
     */
    public static String generaChareId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
