package com.huatu.tiku.match.service.v1.report;

import com.huatu.ztk.commons.exception.BizException;

/**
 * 描述：我的报告接口
 *
 * @author biguodong
 * Create time 2018-12-27 下午5:36
 **/
public interface ReportService {

    /**
     * 查询模考大赛报告列表
     *
     * @param userId
     * @param tagId
     * @param subject
     * @return
     * @throws BizException
     */
    Object myReportList(int userId, int tagId, int subject,String cv,int terminal) throws BizException;
}
