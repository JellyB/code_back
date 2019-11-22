package com.huatu.ztk.backend.arena.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户竞技排名bean
 * Created by linkang on 11/18/16.
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class UserRank {
    private int winCount;//胜利场次
    private int failCount;//失败场次
    private String date; //竞技日期
}
