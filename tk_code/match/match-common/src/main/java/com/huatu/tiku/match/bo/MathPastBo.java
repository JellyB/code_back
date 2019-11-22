package com.huatu.tiku.match.bo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2018-12-28 下午5:50
 **/

@Data
@NoArgsConstructor
public class MathPastBo implements Serializable {

    /**
     * 试卷id
     */
    private int matchId;

    /**
     * 大赛名称
     */
    private String name;

    /**
     * 时间信息
     */
    @JsonIgnore
    private String timeInfo;

    /**
     * 总共多少人作答
     */
    private int answerCount;

    /**
     * 0 未完成 - 继续答题
     * 1 已完成 - 开始答题
     */
    private int answerStatus;

    /**
     * 我的完成次数
     */
    private int completeCount;

    @Builder
    public MathPastBo(int matchId, String name, String timeInfo, int answerCount, int answerStatus, int completeCount) {
        this.matchId = matchId;
        this.name = name;
        this.timeInfo = timeInfo;
        this.answerCount = answerCount;
        this.answerStatus = answerStatus;
        this.completeCount = completeCount;
    }
}
