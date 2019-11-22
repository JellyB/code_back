package com.huatu.tiku.course.service.v5;


import com.huatu.common.exception.BizException;

import java.util.Map;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-09-27 4:26 PM
 **/
public interface CommonServiceV5Biz {

    /**
     * pc 听课获取课后作业信息
     * @param params
     * @param userId
     * @param syllabusId
     * @param subjectType
     * @param buildType
     * @param afterCoreseNum
     * @return
     */
    Object classToken(Map<String, Object> params, int userId, long syllabusId, int subjectType, int buildType, int afterCoreseNum ) throws BizException;
}
