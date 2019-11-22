package com.huatu.ztk.paper.bo;

import com.huatu.ztk.knowledge.bean.Module;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 答题卡做题信息
 * Created by huangqingpeng on 2019/2/19.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class StandardCardSimpleBo {
    /**
     * 答题卡ID
     */
    private long practiceId;
    /**
     * 模考名称
     */
    private String name;
    /**
     * 剩余时间（秒）
     */
    private int remainTime;
    /**
     * 模块信息
     */
    private List<Module> modules;
    /**
     * 试题ID
     */
    private List<Integer> questions;
    /**
     * 试题答案正确与否（0未做1正确2错误）
     */
    private int[] corrects;
    /**
     * 答案（默认‘0’）
     */
    private String[] answers;
    /**
     * 是否有疑问（0无1有）
     */
    private int[] doubts;
    /**
     * 单个试题耗时
     */
    private int[] times;
}
