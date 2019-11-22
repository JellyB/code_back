package com.huatu.tiku.essay.essayEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/6/4
 * @描述
 */
@AllArgsConstructor
@Getter
public enum MockTagEnum {


    essay_2020(26, "2020申论模考", true),
    essay_2019(3, "2019申论模考", true),
    essay_2018(3, "2018申论模考", false);

    private int code;
    private String name;
    private Boolean flag;//现在是否展示,false现在不展示

}
