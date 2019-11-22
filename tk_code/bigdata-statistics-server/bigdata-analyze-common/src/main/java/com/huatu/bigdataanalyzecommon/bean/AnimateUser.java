package com.huatu.bigdataanalyzecommon.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class AnimateUser {


    //查询条件
    private String conditionKey;

    //是否等于
    private Integer isEqual;

    //查询条件值
    private String conditionValue;

    private String result;

    @Override
    public String toString() {

        return "{\"conditionKey\":\"" + conditionKey + "\"" + ',' +
                "\"isEqual\":" + isEqual + ',' +
                "\"conditionValue\":\"" + conditionValue + "\"}";
    }
}
