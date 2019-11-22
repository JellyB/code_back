package com.huatu.tiku.match.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by huangqingpeng on 2019/3/1.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MatchTestBean {
    private int id;
    private int userId;
    private String uname;
    private long practiceId;
    private int positionId;
}
