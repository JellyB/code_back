package com.huatu.ztk.backend.paper.bean;

import lombok.Data;

import java.util.List;

/**
 * Created by linkang on 17-7-7.
 */
@Data
public class SmartPaperBean {
    private int paperId;
    private int[] difficultRatio;
    private int[] publishRatio;
    private List<SmartPaperModuleBean> modules;
}
