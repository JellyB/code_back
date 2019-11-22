package com.huatu.ztk.paper.service.v4;

import java.util.HashMap;
import java.util.List;

import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.paper.vo.PracticeReportVo;

/**
 * @author shanjigang
 * @date 2019/3/15 14:33
 */
public interface PracticeCourseService {
    /**
     * 查看随堂练习报告
     * @param courseId
     * @return
     */
    PracticeReportVo getPracticeReport(Long courseId,int type,int subject,Long userId,Integer playType)throws BizException;

    /**
     * 批量查询随堂练习报告生成状态
     * @param courseMap
     * @return
     */
    List<HashMap<String,Object>> getBatchCoursePracticeStatus(Long userId,List<HashMap<String,Object>> courseMap);

}
