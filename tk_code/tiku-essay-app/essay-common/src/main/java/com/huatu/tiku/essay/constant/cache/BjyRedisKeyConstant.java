package com.huatu.tiku.essay.constant.cache;

import com.google.common.base.Joiner;

/**
 * @author zhaoxi
 * @Description: 百家云视频相关缓存
 * @date 2018/11/5下午4:13
 */
public class BjyRedisKeyConstant {

    public static String getVideoTokenKey(Integer videoId) {
        return Joiner.on("_").join("bjy_video_token",videoId);

    }
}
