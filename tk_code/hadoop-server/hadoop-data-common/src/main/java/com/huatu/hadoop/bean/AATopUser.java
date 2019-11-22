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
public class AATopUser {

    private int userId;
    private int nowWeekRank;
    private int lastWeekRank;
    private double prediceScore;

    @Override
    public String toString() {

        return JSON.toJSONString(this, true);
    }
}
