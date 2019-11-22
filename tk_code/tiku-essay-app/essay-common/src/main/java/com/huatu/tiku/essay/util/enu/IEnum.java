package com.huatu.tiku.essay.util.enu;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by duanxiangchao on 2019/7/9
 */
@JsonFormat(
        shape = JsonFormat.Shape.OBJECT
)
public interface IEnum<T> {

    @JsonIgnore
    T getValue();

    String getTitle();

    default String getName() {
        Enum<?> temp = (Enum)this;
        return temp.name();
    }

}
