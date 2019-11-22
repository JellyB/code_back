package com.huatu.tiku.response.paper;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 试卷详情查询数据
 * Created by x6 on 2018/7/27.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SelectPaperEntityInfoResp {
    /**
     * 试卷id
     */
    private Long paperId;
    /**
     * 试卷名称
     */
    private String paperName;
    /**
     * 试卷类表- 0 or 1
     */
    private Integer mode;
    /**
     * 试卷类别-真题or模拟题
     */
    private String modeName;
    /**
     * 年份
     */
    private Integer year;
    /**
     * 地区id
     */
    private List<Long> areaIds;
    /**
     * 地区名称
     */
    private List<String> areaName;
    /**
     * 试题状态（发布or未发布）
     */
    private String status;
    /**
     * 试题状态（1 or 2）
     */
    private Integer bizStatus;
    /**
     * 是否残缺 1残缺 2 正常
     */
    private Integer missFlag;
    private String missStatus;
    /**
     * 残缺题数
     */
    private Integer missCount;
    /**
     * 题数
     */
    private Integer qCount;
    /**
     * 模块信息
     */
    private List<SelectModuleResp> modules;
}
