package com.ht.galaxy.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jbzm
 * @date Create on 2018/4/12 13:42
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchListVo {
    /**
     * 模考名称
     */
    private String matchName;
    /**
     * moatchId
     */
    private String matchId;
}
