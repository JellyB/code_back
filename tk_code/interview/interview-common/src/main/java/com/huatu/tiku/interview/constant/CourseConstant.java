package com.huatu.tiku.interview.constant;

import lombok.AllArgsConstructor;

/**
 * @Author jbzm
 * @Date Create on 2018/1/17 11:57
 */
@AllArgsConstructor
public enum CourseConstant {

    IMG_FILE_BASE_BATH("/var/www/cdn/images/vhuatu/interview/arrangement/"),

    IMG_BASE_URL("http://tiku.huatu.com/cdn/images/vhuatu/interview/arrangement/");
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
