package com.huatu.ztk.paper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author shanjigang
 * @date 2019/3/5 14:55
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class ScoreRankDto {
    /**
     * 答题用时
     */
    private int expendTime;

    /**
     * 用户头像
     */
    private String icon;

    /**
     * 排名
     */
    private int rank;

    /**
     * 成绩
     */
    private int score;

    /**
     * 交卷时间
     */
    private Long submitTime;

    /**
     * 用户名
     */
    private String userName;
    
    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 用户Id
     */
    private Long userId;
}
