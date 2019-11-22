package com.huatu.ztk.user.common;

/**
 * 支持的头像文件类型
 * Created by linkang on 9/12/16.
 */

public enum AvatarFileType {
    JPG("FFD8FF","jpg"),
    PNG("89504E47","png"),
    GIF("47494638","gif");

    private String value = "";
    private String suffix = "";

    AvatarFileType(String value, String suffix) {
        this.value = value;
        this.suffix = suffix;
    }

    public String getValue() {
        return value;
    }

    public String getSuffix() {
        return suffix;
    }
}
