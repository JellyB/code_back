package com.huatu.tiku.essay.essayEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author huangqingpeng
 * @title: CorrectModeEnum
 * @description: 批改模式类型
 * @date 2019-07-0913:17
 */
@AllArgsConstructor
@Getter
public enum  CorrectModeEnum {
    INTELLIGENCE(1, "智能批改"),
    MANUAL(2, "人工批改"),
    INTELLIGENCE_2_MANUAL(3,"智能转人工批改");

    private Integer mode;
    private String name;


    public static CorrectModeEnum create(Integer correctMode) {
        for (CorrectModeEnum value : CorrectModeEnum.values()) {
            if(value.getMode() == correctMode){
                return value;
            }
        }
        return INTELLIGENCE;
    }

    /**
     * 如果匹配不到返回 null
     * @param correctMode
     * @return
     */
    public static CorrectModeEnum createDefaultNull(Integer correctMode) {
        for (CorrectModeEnum value : CorrectModeEnum.values()) {
            if(value.getMode() == correctMode){
                return value;
            }
        }
        return null;
    }
}
