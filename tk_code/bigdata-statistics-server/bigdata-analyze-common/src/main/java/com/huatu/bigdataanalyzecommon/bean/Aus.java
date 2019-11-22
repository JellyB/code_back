package com.huatu.bigdataanalyzecommon.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class Aus {

    private List<AnimateUser> aus;

    @Override
    public String toString() {
        return "{\"usa\":" + aus +
                "}";
    }
}
