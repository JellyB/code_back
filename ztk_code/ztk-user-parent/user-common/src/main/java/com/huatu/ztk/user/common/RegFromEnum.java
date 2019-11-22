package com.huatu.ztk.user.common;

import com.huatu.ztk.commons.TerminalType;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-04-29 3:23 PM
 **/
@Getter
@AllArgsConstructor
public enum RegFromEnum {
    ANDROID(String.valueOf(TerminalType.ANDROID), "安卓"),
    IPHONE(String.valueOf(TerminalType.IPHONE), "苹果"),
    PC(String.valueOf(TerminalType.PC), "pc"),
    ANDROID_IPAD(String.valueOf(TerminalType.ANDROID_IPAD), "安卓pad"),
    IPHONE_IPAD(String.valueOf(TerminalType.IPHONE_IPAD), "苹果pad"),
    WEI_XIN(String.valueOf(TerminalType.WEI_XIN), "微信"),
    MOBILE(String.valueOf(TerminalType.MOBILE), "M站"),
    WEI_XIN_APPLET(String.valueOf(TerminalType.WEI_XIN_APPLET), "小程序"),
    REGISTER_USER_PHP("8", "php批量注册"),
    EDUCATION_SYNC("9", "教育同步"),
    OTHERS("10", "其他");


    private String from;
    private String value;

    public static RegFromEnum create(String regFrom) {
        for (RegFromEnum regFromEnum : values()) {
            if (regFromEnum.getFrom().equals(regFrom)) {
                return regFromEnum;
            }
        }
        return RegFromEnum.OTHERS;
    }
}
