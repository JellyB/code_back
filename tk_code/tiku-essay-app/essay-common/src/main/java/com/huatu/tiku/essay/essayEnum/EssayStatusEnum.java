package com.huatu.tiku.essay.essayEnum;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-07-02 10:34 AM
 **/
public enum EssayStatusEnum {

    NORMAL(1, "正常"), DELETED(-1, "逻辑删除");

    private int code;

    private String text;

    EssayStatusEnum(int code, String text) {
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
