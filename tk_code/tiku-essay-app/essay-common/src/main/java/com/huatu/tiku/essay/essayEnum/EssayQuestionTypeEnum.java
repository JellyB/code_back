package com.huatu.tiku.essay.essayEnum;

import com.huatu.tiku.essay.util.enu.EnumUtils;
import com.huatu.tiku.essay.util.enu.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/8/27
 * @描述
 */
@AllArgsConstructor
@Getter
public enum EssayQuestionTypeEnum implements IEnum<Integer> {

    GNGK(1,"概括归纳"),
    ZHFX(2,"综合分析"),
    TCDC(3,"解决问题"),
    YYXZ(4,"应用写作"),
    YLWX(5,"文章写作");

    private Integer value;
    private String title;

    @Override
    public Integer getValue() {
        return this.value;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    public static EssayQuestionTypeEnum create(Integer value) {
        return (EssayQuestionTypeEnum)EnumUtils.getEnum(values(), value);
    }

}
