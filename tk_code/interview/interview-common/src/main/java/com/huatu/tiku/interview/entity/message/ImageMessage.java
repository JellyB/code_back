package com.huatu.tiku.interview.entity.message;

import lombok.Data;

/**
 * @Author: ZhenYang
 * @Date: Created in 2018/1/11 19:49
 * @Modefied By:
 * 图片消息
 */
@Data
public class ImageMessage extends BaseMessage {
    // 图片链接
    private String PicUrl;

    public String getPicUrl() {
        return PicUrl;
    }

    public void setPicUrl(String picUrl) {
        PicUrl = picUrl;
    }
}