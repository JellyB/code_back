package com.huatu.tiku.match.bo.paper;

import com.huatu.ztk.knowledge.bean.Module;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by lijun on 2019/1/8
 */
@Data
@NoArgsConstructor
public class StandAnswerCardSimpleBo extends AnswerCardSimpleBo {

    //试卷ID
    private Integer paperId;

    //模块列表
    List<Module> modules;

    //答题记录
    private String[] answers;
    //是否正确
    private int[] corrects;
    //每道题的耗时 单位是秒
    private int[] times;
    //是否有疑问
    private int[] doubts;

    //最后答题位置
    private Integer lastIndex;

    @Builder
    public StandAnswerCardSimpleBo(Long id, String idStr, String name, Integer expendTime, Integer remainingTime, Long startTime, Long endTime, Long currentTime, String matchErrorPath, Integer paperId, List<Module> modules, String[] answers, int[] corrects, int[] times, int[] doubts, Integer lastIndex) {
        super(id, idStr, name, expendTime, remainingTime, startTime, endTime, currentTime, matchErrorPath);
        this.paperId = paperId;
        this.modules = modules;
        this.answers = answers;
        this.corrects = corrects;
        this.times = times;
        this.doubts = doubts;
        this.lastIndex = lastIndex;
    }
}
