package com.huatu.tiku.match.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

/**
 * @author huangqingpeng
 * 模考大赛用户首页数据返回数据结构
 */
@Builder
@AllArgsConstructor
@Data
public class MatchHeadUserBo {

    private int matchId;

    private int positionId;

    private int enrollCount;

    private long practiceId;

    private int status;
}
