package com.huatu.tiku.interview.service;

import com.huatu.tiku.interview.entity.vo.response.PaperDetailVO;

/**
 * Created by x6 on 2018/4/11.
 */
public interface LearningSituationMaterialService {

    Object getPracticeContent();

    Object getRemarkList(long typeId);

    Object getWordList();

    Object getExpressionList();

    Object getPaperDetail();
    PaperDetailVO getPaperDetailById(long id);
}
