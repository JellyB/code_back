package com.huatu.tiku.essay.essayEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.ExcludeDefaultListeners;

/**
 * @author huangqingpeng
 * @title: CommonOperateEnum
 * @description: TODO
 * @date 2019-07-2317:31
 */
@AllArgsConstructor
@Getter
public enum CommonOperateEnum {
    CREATE("c"),
    READ("r"),
    UPDATE("u"),
    DELETE("d"),
    ;

    private String value;

    public static CommonOperateEnum create(String value){
        for (CommonOperateEnum commonOperateEnum : CommonOperateEnum.values()) {
            if(commonOperateEnum.getValue().equalsIgnoreCase(value)){
                return commonOperateEnum;
            }
        }
        return READ;
    }
}
