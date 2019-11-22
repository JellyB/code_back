package com.huatu.tiku.course.service.v1;


import lombok.Builder;

import java.util.Map;

/**
 * 描述：数据上报 service
 *
 * @author biguodong
 * Create time 2019-02-22 上午11:16
 **/
public interface ProcessReportService{



    /**
     * 录播数据回放
     * @param params
     * @return
     */
    Object playBackReport(Map<String,Object> params);



    Object liveReport();
}
