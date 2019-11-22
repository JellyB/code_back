package com.huatu.tiku.essay.essayEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 描述：大纲类型枚举
 *
 * @author biguodong
 * Create time 2019-03-21 3:27 PM
 **/

@AllArgsConstructor
@Getter
public enum SyllabusTypeEnum {

    /**
     * 阶段
     */
    STAGE("阶段", 0),
    /**
     * 小课
     */
    COURSE("课程", 1),
    /**
     * 课件类型
     */
    COURSE_WARE("课件", 2);
    private String value;
    private int type;

    public static SyllabusTypeEnum create(int type){
        for (SyllabusTypeEnum typeEnum : values()) {
            if(typeEnum.getType() == type){
                return typeEnum;
            }
        }
        return null;
    }

}
