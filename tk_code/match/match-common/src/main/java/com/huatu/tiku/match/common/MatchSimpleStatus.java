package com.huatu.tiku.match.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author huangqingpeng
 * 用户模考大赛首页原始数据（实时维护）
 */
@AllArgsConstructor
@Builder
@Getter
@Setter
public class MatchSimpleStatus {

    private int matchId;

    private int userId;

    private int enrollCount;

    private int positionId;

    private long practiceId;

    private int submitFlag;
}


