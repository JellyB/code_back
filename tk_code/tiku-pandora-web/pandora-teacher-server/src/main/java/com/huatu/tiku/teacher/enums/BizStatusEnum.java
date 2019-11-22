package com.huatu.tiku.teacher.enums;


import com.huatu.tiku.enumHelper.EnumUtils;
import com.huatu.tiku.enumHelper.IEnum;

/**
 * Created by huangqp on 2018\5\18 0018.
 */
public enum BizStatusEnum implements IEnum<Integer> {
    NO_PUBLISH(1,"未发布"),
    PUBLISH(2,"发布")
    ;

    private Integer status;
    private String name;

    public static BizStatusEnum create(Integer value) {
        return EnumUtils.getEnum(BizStatusEnum.values(), value);
    }
    BizStatusEnum(Integer status, String name){
        this.status = status;
        this.name = name;
    }
    @Override
    public Integer getValue() {
        return status;
    }

    @Override
    public String getTitle() {
        return name;
    }
}

