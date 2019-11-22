package com.huatu.ztk.paper.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by linkang on 7/18/16.
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class PaperMeta implements Serializable {
    private static final long serialVersionUID = 1L;

    private int cardCounts;   //交卷人数,答题卡人数
}
