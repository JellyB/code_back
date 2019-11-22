package com.huatu.ztk.paper.enums;

import com.huatu.ztk.paper.common.PaperRedisKeys;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by huangqingpeng on 2019/3/7.
 */
@AllArgsConstructor
@Getter
public enum ScoreSortEnum {
    SCORE_SORT(1,"按分数排名"),
    SCORE_SUBMIT_SORT(2,"按分数+提交时间排名"),
    SCORE_EXPEND_SORT(3,"按分数+耗时排名");

    private int code;

    private String name;


    public String getScoreSortCacheKey(int paperId) {
        String paperPracticeIdSore = PaperRedisKeys.getPaperPracticeIdSore(paperId);
        return paperPracticeIdSore + "_" + this.getCode();
    }
}
