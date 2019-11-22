package com.huatu.tiku.teacher.service.download.v1;

import com.huatu.tiku.entity.download.BaseTool;
import com.huatu.tiku.entity.teacher.PaperModuleInfo;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.enums.QuestionElementEnum;
import com.huatu.tiku.teacher.service.impl.download.v1.PdfWriteServiceImplV1;
import com.huatu.ztk.paper.bean.PracticeCard;
import com.huatu.ztk.question.bean.Question;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * word 写入服务
 * Created by huangqingpeng on 2018/8/15.
 */
public interface DownloadWriteServiceV1 {


    /**
     * 获取需要下载的数据信息
     * @param question
     * @param sort
     * @return
     */
    Map<String, Object> transMapData(Question question, Integer sort);

    String makeWordByPaper(long paperId, BaseTool baseTool, BiFunction<BaseTool, String, BaseTool> initWriteTool, BiConsumer<BaseTool, String> titleWrite, BiConsumer<BaseTool, PaperModuleInfo> moduleWrite, BiConsumer<BaseTool, Map<String, Object>> questionWrite);

    /**
     * 获得试卷的所有模块名称
     * @param ids    试卷Id
     * @param typeInfo
     */
    List<String> getModuleNames(List<Long> ids, PaperInfoEnum.TypeInfo typeInfo);

    /**
     * 查询试卷下某个模块下 的试题ID(做去重)
     * @param ids
     * @param typeInfo
     * @param duplicateFlag
     * @param moduleName
     * @return
     */
    List<Integer> getQuestionByModule(List<Long> ids, PaperInfoEnum.TypeInfo typeInfo, boolean duplicateFlag, String moduleName);

    /**
     * 之写入试题信息
     * @param tool
     * @param writeQuestion
     * @param questionIds
     * @param exportType
     */
    void writeQuestions(BaseTool tool, BiConsumer<BaseTool, Map<String, Object>> writeQuestion, List<Integer> questionIds, QuestionElementEnum.QuestionFieldEnum exportType);

    String makeWordByPracticeCard(PracticeCard practiceCard, PdfWriteServiceImplV1.PdfWriteTool pdfWriteTool, BiFunction<BaseTool, String, BaseTool> writeTool, BiConsumer<BaseTool, String> writeTitle, BiConsumer<BaseTool, PaperModuleInfo> writeModule, BiConsumer<BaseTool, Map<String, Object>> writeQuestion);
}
