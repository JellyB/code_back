package com.huatu.tiku.teacher.service;

import com.huatu.ztk.paper.bean.Paper;

/**
 * mongo ztk_paper
 * Created by huangqp on 2018\7\6 0006.
 */
public interface OldPaperService {
    /**
     * 试卷查询
     *
     * @param paperId
     * @return mongo-paper
     */
    Paper findPaperById(Integer paperId);
}

