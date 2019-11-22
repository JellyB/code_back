package com.huatu.tiku.interview.entity.message;

import lombok.Data;

/**
 * @Author: ZhenYang
 * @Date: Created in 2018/1/11 19:50
 * @Modefied By:
 * 地理位置消息
 */
@Data
public class LocationMessage extends BaseMessage {
    // 地理位置维度
    private String Location_X;
    // 地理位置经度
    private String Location_Y;
    // 地图缩放大小
    private String Scale;
    // 地理位置信息
    private String Label;
}