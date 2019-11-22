package com.huatu.tiku.course.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-03-23 8:04 PM
 **/

@AllArgsConstructor
@Getter
public enum  LiveStatusEnum {

    UN_START(0, "未开始"),
    ON_LIVE(1, "直播中"),
    FINISHED(2, "已经结束");

    private int code;
    private String value;

    public static LiveStatusEnum create(int liveStatus){
        for (LiveStatusEnum liveStatusEnum : values()) {
            if(liveStatusEnum.getCode() == liveStatus){
                return liveStatusEnum;
            }
        }
        return null;
    }
}
