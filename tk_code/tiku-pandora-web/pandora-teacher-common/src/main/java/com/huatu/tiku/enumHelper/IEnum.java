package com.huatu.tiku.enumHelper;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Created by duanxiangchao on 2018/5/2
 */
@JsonSerialize(converter = EnumResponseConverter.class)
public interface IEnum <T> {

    T getValue();

    String getTitle();

}
