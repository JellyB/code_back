package com.huatu.tiku.essay.constant.cache;

import com.google.common.base.Joiner;

/**
 * redis相关key管理
 * @author zhaoxi
 */
public class CommonRedisKeyConstant {

    /**
     * 用户收藏题目信息
     * @return
     */
    public static String  getPaperListOfAreaKey(long areaId) {
        return Joiner.on("_").join("paper_list_of_area",areaId);
    }
}
