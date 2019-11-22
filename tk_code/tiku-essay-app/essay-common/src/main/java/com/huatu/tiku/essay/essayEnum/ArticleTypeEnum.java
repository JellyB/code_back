package com.huatu.tiku.essay.essayEnum;

import com.huatu.tiku.essay.util.enu.EnumUtils;
import com.huatu.tiku.essay.util.enu.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhangchong 要点制划档制枚举
 */
@AllArgsConstructor
@Getter
public enum ArticleTypeEnum implements IEnum<Integer> {

    MAINPOINT(1, "要点制"), CATEGORY(2, "划档制");

    private int type;
    private String name;

    public static ArticleTypeEnum create(int comprehensiveCorrectType, int type) {

        switch (type) {
            case 4:         //应用文，制度不固定，默认要点制
                if (CATEGORY.getType() == comprehensiveCorrectType) {
                    return CATEGORY;
                }
                return MAINPOINT;
            case 5:     //议论文划档制
                return CATEGORY;
            default:        //小题都是要点制
                return MAINPOINT;
        }

    }

    public static ArticleTypeEnum create(Integer value) {
        return (ArticleTypeEnum) EnumUtils.getEnum(values(), value);
    }

    @Override
    public Integer getValue() {
        return this.type;
    }

    @Override
    public String getTitle() {
        return this.name;
    }
}
