package com.huatu.bigdataanalyzecommon.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class AnimateInfo {

    private String info;
    private String result;
}
