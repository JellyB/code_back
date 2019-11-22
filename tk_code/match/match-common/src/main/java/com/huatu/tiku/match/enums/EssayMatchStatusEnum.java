package com.huatu.tiku.match.enums;

import com.huatu.tiku.match.enums.util.EnumCommon;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by huangqingpeng on 2018/10/19.
 */
@Getter
@AllArgsConstructor
public enum EssayMatchStatusEnum implements EnumCommon {
    //1未完成2已交卷3已批改
    UNFINISH(1, "未完成"),
    SUBMITTED(2, "已交卷"),
    CORRECTED(3, "已批改"),
    DEFAULT(-1,"未知")
    ;

    private int key;
    private String value;

    @Override
    public int getKey() {
        return this.key;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    public EssayMatchStatusEnum getDefault(){
        return DEFAULT;
    }

    public static EssayMatchStatusEnum create(int key){
        for (EssayMatchStatusEnum essayMatchStatusEnum : EssayMatchStatusEnum.values()) {
            if(essayMatchStatusEnum.getKey() == key){
                return essayMatchStatusEnum;
            }
        }
        return DEFAULT;
    }
}
