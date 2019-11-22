package com.huatu.tiku.match.service.v1.search;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-01-09 下午5:44
 **/
public interface CourseService {

    /**
     * 根据classId获取课程信息
     * @param classId
     * @return
     */
    Object courseInfo(int classId);
}
