package com.huatu.ztk.backend.metas.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by huangqp on 2018\3\20 0020.
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class MatchBean {
    private int paperId;
    private String name;
    private long startTime;
    private long endTime;
}
