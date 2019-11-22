package com.huatu.ztk.user.common;

import lombok.AllArgsConstructor;

/**
 * @Author ZhenYang
 * @Date Created in 2018/2/2 20:10
 * @Description
 */
@AllArgsConstructor
public enum TicklingConstant {
    IMG_FILE_BASE_BATH("/var/www/cdn/images/vhuatu/tiku/tickling/"),

    LOG_FILE_BASE_BATH("/var/www/cdn/logs/"),

    IMG_BASE_URL("http://tiku.huatu.com/cdn/images/vhuatu/tiku/tickling/"),

    LOG_BASE_URL("http://tiku.huatu.com/cdn/logs/");

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
