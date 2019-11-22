package com.huatu.tiku.match.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author huangqingpeng
 *  用户模考大赛首页数据
 */
@AllArgsConstructor
@Builder
@Data
public class MatchHeadUserInfo {

    private int subjectId;

    private int userId;

    private List<MatchSimpleStatus> userStatus;
}
