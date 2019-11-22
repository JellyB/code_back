package com.huatu.tiku.essay.constant.status;

import lombok.AllArgsConstructor;

/**
 * @Author ZhenYang
 * @Date Created in 2018/2/2 20:10
 * @Description
 */
@AllArgsConstructor
public enum TicklingConstant {
    IMG_FILE_BASE_BATH("/var/www/cdn/images/vhuatu/tiku/tickling/"),

    IMG_BASE_URL("http://tiku.huatu.com/cdn/images/vhuatu/tiku/tickling/");

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
