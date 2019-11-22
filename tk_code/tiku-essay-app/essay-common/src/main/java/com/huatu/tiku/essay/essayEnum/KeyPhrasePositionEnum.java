package com.huatu.tiku.essay.essayEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/9/27
 * @描述
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum KeyPhrasePositionEnum {

    WHOLE_CHAPTER(1, "全篇"),
    START(2, "开头"),
    START_OR_CENTER(3, "开头或中间"),
    CENTER(4, "中间"),
    center_or_end(5, "中间或结尾"),
    END(6, "结尾");

    private Integer code;
    private String desc;

    public static KeyPhrasePositionEnum getKeyPhrasePosition(int code) {
        KeyPhrasePositionEnum[] values = KeyPhrasePositionEnum.values();
        for (KeyPhrasePositionEnum positionEnum : values) {
            if (code == positionEnum.getCode()) {
                return positionEnum;
            }
        }
        return WHOLE_CHAPTER;
    }
}
