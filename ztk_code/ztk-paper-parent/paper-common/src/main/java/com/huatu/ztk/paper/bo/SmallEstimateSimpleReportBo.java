package com.huatu.ztk.paper.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by huangqingpeng on 2019/2/18.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmallEstimateSimpleReportBo {

    /**
     * 答题卡Id
     */
    private long practiceId;

    /**
     * 答题卡ID字符串（PC端专用）
     */
    private String idStr;

    /**
     * 小模考名称
     */
    private String name;

    /**
     * 试题量
     */
    private int qCount;

    /**
     * 完成人数
     */
    private int submitCount;

    /**
     * 击败比例
     */
    private int beatRate;
}


