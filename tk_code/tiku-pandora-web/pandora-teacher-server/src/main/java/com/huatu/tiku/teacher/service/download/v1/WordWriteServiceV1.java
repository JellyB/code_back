package com.huatu.tiku.teacher.service.download.v1;

import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.enums.QuestionElementEnum;
import com.huatu.ztk.commons.exception.BizException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;

import java.util.List;
import java.util.Map;

/**
 * document 写入元素
 * Created by huangqingpeng on 2018/8/15.
 */
public interface WordWriteServiceV1 {

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
     * @param moduleName
     * @param duplicateFlag 是否去重
     * @return
     */
    String downLoadList(List<Long> paperIds, PaperInfoEnum.TypeInfo typeInfo, QuestionElementEnum.QuestionFieldEnum exportType, String moduleName, boolean duplicateFlag) throws BizException;

    /**
     * 导出word -- 添加试题元素
     *
     * @param document
     * @param font
     * @param questionMap
     */
    void addQuestionElement(Document document, Font font, Map<String, Object> questionMap, QuestionElementEnum.QuestionFieldEnum exportType) throws Exception;

    /**
     * 导出word -- 添加模块元素
     *
     * @param document
     * @param moduleName
     * @param sort
     */
    void addModuleElement(Document document, String moduleName, int sort) throws DocumentException;

    /**
     * 导出word -- 添加标题元素
     *
     * @param document
     * @param title
     */
    void addTitleElement(Document document, String title) throws DocumentException;

    /**
     * 批量导出的试卷按照模块合并分组
     *
     * @param ids
     * @param typeInfo
     * @param questionFieldEnum
     * @return
     */
    String downloadGroupByModule(List<Long> ids, PaperInfoEnum.TypeInfo typeInfo, QuestionElementEnum.QuestionFieldEnum questionFieldEnum) throws BizException;

    /**
     * 下载知识点下错误率前50的试题
     *
     * @param pointId           知识点ID
     * @param size              要下载的题量（top num）
     * @param questionFieldEnum 试题下载模式
     * @return
     */
    String downloadTopPoint(Long pointId, int size, QuestionElementEnum.QuestionFieldEnum questionFieldEnum);

    /**
     * 根据试题ID，导出试卷，试卷名为知识点名称
     * @param pointId
     * @param ids
     * @param questionFieldEnum
     * @return
     */
    String downloadByQuestionWithKnowledge(Long pointId, String ids, QuestionElementEnum.QuestionFieldEnum questionFieldEnum) throws BizException;

}
