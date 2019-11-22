package com.huatu.tiku.match.service.v1.paper;

import com.huatu.tiku.match.enums.PaperInfoEnum;
import com.huatu.ztk.paper.bean.Paper;

import java.util.List;


/**
 * 试卷信息
 * Created by lijun on 2018/10/19
 */
public interface PaperService {

    /**
     * 通过试卷ID 查询试卷信息
     *
     * @param paperId 试卷ID
     * @return 试卷信息
     */
    Paper findPaperCacheById(int paperId);

    /**
     * 获取答题卡对应的试题ID List
     *
     * @param paperId 试卷ID
     * @return 试题ID 集合
     */
    List<Integer> getPaperQuestionIdList(int paperId);

    /**
     * 通过试卷ID 获取试卷类型
     *
     * @param paperId 试卷ID
     * @return 试卷类型
     */
    PaperInfoEnum.PaperTypeEnum getPaperTypeById(int paperId);


}
