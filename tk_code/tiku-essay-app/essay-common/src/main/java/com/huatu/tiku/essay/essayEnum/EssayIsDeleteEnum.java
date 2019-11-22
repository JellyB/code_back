package com.huatu.tiku.essay.essayEnum;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-07-02 2:55 PM
 **/
public enum  EssayIsDeleteEnum {

    NORMAL(0, "表示为修改"), DELETED(1, "表示为删除");

    private int code;

    private String text;

    EssayIsDeleteEnum(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
