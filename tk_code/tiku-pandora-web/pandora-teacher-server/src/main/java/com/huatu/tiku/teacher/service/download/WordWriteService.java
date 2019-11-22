package com.huatu.tiku.teacher.service.download;

import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.enums.QuestionElementEnum;

import java.util.List;

/**
 * word 写入服务
 * Created by huangqingpeng on 2018/8/15.
 */
public interface WordWriteService {

    /**
     * 根据试卷id,下载试卷word信息（支持实体卷和活动卷）
     *
     * @param paperId
     * @param typeInfo
     * @param exportType
     * @return
     */
    String download(long paperId, PaperInfoEnum.TypeInfo typeInfo, QuestionElementEnum.QuestionFieldEnum exportType);

    /**
     * 批量下载试卷
     *
     * @param paperIds
     * @param typeInfo
     * @param exportType
     * @return
     */
    String downLoadList(List<Long> paperIds, PaperInfoEnum.TypeInfo typeInfo, QuestionElementEnum.QuestionFieldEnum exportType);
}
