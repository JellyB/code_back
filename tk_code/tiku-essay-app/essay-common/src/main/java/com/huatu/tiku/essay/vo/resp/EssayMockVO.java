package com.huatu.tiku.essay.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by x6 on 2017/12/15.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EssayMockVO {
    //redis的key
    private String mockRedisKey;

    //答题卡id
    private long answerCardId;

    //考试类型（是不是模考） 1;//真題     0;模考題
    private int examType;

    //用户id
    private int userId;

    //终端类型
    private int terminal;

    //答题卡id
    private long paperId;




}
