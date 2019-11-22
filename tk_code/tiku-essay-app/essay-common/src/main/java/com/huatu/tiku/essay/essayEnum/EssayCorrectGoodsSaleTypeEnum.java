package com.huatu.tiku.essay.essayEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 批改商品售卖类型
 *
 * @author geek-s
 * @date 2019-07-22
 */
@Getter
@AllArgsConstructor
public enum EssayCorrectGoodsSaleTypeEnum {

    APP_SALE("APP售卖"), COURSE_GIFT("课程赠送");

    private String value;

    public static EssayCorrectGoodsSaleTypeEnum of(Integer value) {
        for (EssayCorrectGoodsSaleTypeEnum saleTypeEnum : values()) {
            if (saleTypeEnum.ordinal() == value) {
                return saleTypeEnum;
            }
        }

        return null;
    }
}
