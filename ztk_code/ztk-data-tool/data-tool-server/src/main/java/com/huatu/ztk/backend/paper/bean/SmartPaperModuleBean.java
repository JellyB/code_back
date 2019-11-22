package com.huatu.ztk.backend.paper.bean;

import lombok.Data;

import java.util.List;

/**
 * Created by linkang on 17-7-7.
 */
@Data
public class SmartPaperModuleBean {
    private int id;
    private String name;
    private List<SmartPaperModulePoint> points;
}
