package com.huatu.hadoop.bean;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;


@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class PointDetail {

    private Integer pointKey;
    private Integer pointCorrect;
    private Integer pointNum;
    private Integer pointTime;
    private Double accuracy;
    private Double speed;

    @Override
    public String toString() {


        return JSON.toJSONString(this, true);
    }
}
