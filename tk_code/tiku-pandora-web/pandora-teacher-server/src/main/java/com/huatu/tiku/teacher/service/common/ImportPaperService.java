package com.huatu.tiku.teacher.service.common;

import java.util.List;

/**
 * 处理试卷-活动 Mysql-MongoDB 同步
 * Created by lijun on 2018/9/19
 */
public interface ImportPaperService {

    /**
     * 将试卷信息导入到mongo
     *
     * @param paperId 试卷ID
     */
    void importPaper(long paperId);

    /**
     * 将试卷信息导入到mongo
     *
     * @param paperIds 试卷ID
     */
    void importPaper(List<Long> paperIds);


}
