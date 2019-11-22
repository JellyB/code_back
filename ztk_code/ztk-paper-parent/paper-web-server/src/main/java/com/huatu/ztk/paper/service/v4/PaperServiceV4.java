package com.huatu.ztk.paper.service.v4;

import com.huatu.ztk.paper.bean.EstimatePaper;
import com.huatu.ztk.paper.bean.Paper;

import java.util.List;

/**
 * ztk_paper相关
 * Created by huangqingpeng on 2019/2/13.
 */
public interface PaperServiceV4 {

    /**
     * 当天小模考试卷查询
     * @param subject
     * @return
     */
    List<EstimatePaper> getTodaySmallEstimatePaper(int subject);

    /**
     * 试卷查询
     * @param id
     * @return
     */
    Paper findById(int id);

    /**
     * 查询科目下的所有小模考试卷关联的课程ID
     * @param subject
     * @return
     */
    String findAllCourseId(int subject);
}
