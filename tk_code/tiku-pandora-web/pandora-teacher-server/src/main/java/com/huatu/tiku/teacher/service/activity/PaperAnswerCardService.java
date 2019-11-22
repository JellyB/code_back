package com.huatu.tiku.teacher.service.activity;

import com.huatu.ztk.paper.bean.AnswerCard;

import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/5/6
 * @描述
 */
public interface PaperAnswerCardService {

    List<AnswerCard> getUserAnswerCardByPaperId(Long paperId);
}
