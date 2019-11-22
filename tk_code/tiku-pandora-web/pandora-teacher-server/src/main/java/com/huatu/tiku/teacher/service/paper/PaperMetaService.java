package com.huatu.tiku.teacher.service.paper;

/**
 * 试卷统计信息处理接口
 * Created by huangqingpeng on 2019/1/15.
 */
public interface PaperMetaService {

    /**
     * 同步统计数据到缓存表
     * @return
     */
    Object syncMetaCache();
}
