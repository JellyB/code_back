package com.huatu.tiku.enumHelper;

import com.fasterxml.jackson.databind.util.StdConverter;

/**
 * Created by duanxiangchao on 2018/5/2
 */
public class EnumResponseConverter extends StdConverter<IEnum, String> {

    @Override
    public String convert(IEnum iEnum) {
        return null == iEnum ? "" : iEnum.getTitle();
    }


}
