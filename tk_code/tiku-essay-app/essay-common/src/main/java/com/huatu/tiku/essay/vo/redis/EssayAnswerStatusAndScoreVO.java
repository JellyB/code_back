package com.huatu.tiku.essay.vo.redis;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by x6 on 2017/12/29.
 *
 * 模考批改
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EssayAnswerStatusAndScoreVO {

    private int status;
    private double examScore;
    private long answerId;
    private double avgScore;

}
