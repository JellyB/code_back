package com.base.enums;

/**
 * 使用类型
 * Created by junli on 2017/9/8.
 */
public enum UsingTypeEnum {

    in_using(1),
    logic_delete(0),
    physical_delete(-1),;

    int code;

    UsingTypeEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
