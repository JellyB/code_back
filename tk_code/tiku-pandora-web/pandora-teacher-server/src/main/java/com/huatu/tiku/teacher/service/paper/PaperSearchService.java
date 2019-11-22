package com.huatu.tiku.teacher.service.paper;

import com.github.pagehelper.PageInfo;
import com.huatu.tiku.entity.teacher.PaperSearchInfo;

/**
 * Created by lijun on 2018/8/8
 */
public interface PaperSearchService {

    /**
     * 查询一张试卷详情 - 实体卷
     *
     * @param paperId 试卷ID
     * @return 试卷详情
     */
    PaperSearchInfo entityDetail(long paperId);

    /**
     * 查询一张试卷信息
     *
     * @param activityId 试卷ID
     * @return
     */
    PaperSearchInfo entityActivityDetail(long activityId);

    /**
     * 查询试题试卷列表
     *
     * @param mode      试卷类型
     * @param year      年份
     * @param areaIds   区域ID
     * @param paperTime 考试时间
     * @param name      名称
     * @return
     */
    PageInfo entityList(int mode, int year, String areaIds, String paperTime, String name, int page, int pageSize);

}
