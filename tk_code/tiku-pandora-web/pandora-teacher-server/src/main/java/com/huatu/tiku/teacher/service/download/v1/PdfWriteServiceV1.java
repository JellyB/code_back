package com.huatu.tiku.teacher.service.download.v1;

import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.enums.QuestionElementEnum;
import com.huatu.ztk.paper.bean.PracticeCard;
import com.itextpdf.text.DocumentException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by huangqingpeng on 2018/11/6.
 */
public interface PdfWriteServiceV1 {

    /**
     * 根据试卷id,下载试卷word信息（支持实体卷和活动卷）
     *
     * @param paperId
     * @param typeInfo
     * @param exportType
     * @param mapData
     * @return
     */
    String download(long paperId, PaperInfoEnum.TypeInfo typeInfo, QuestionElementEnum.QuestionFieldEnum exportType, Map mapData) throws IOException, DocumentException;

    /**
     * 批量下载试卷
     *
     * @param paperIds
     * @param typeInfo
     * @param exportType
     * @return
     */
    String downLoadList(List<Long> paperIds, PaperInfoEnum.TypeInfo typeInfo, QuestionElementEnum.QuestionFieldEnum exportType);

    Map getCacheDown(Long paperId, int paperType, int exportType);

    void saveDownCache(Long paperId, int paperType, int exportType, Map mapData);

    /**
     * 删除试卷下所有下载缓存
     * @param paperId
     */
    void delDownCache(Long paperId,int paperType);

    String downloadByPracticePaper(PracticeCard practiceCard);
}
