package com.huatu.tiku.teacher.service.impl.download;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.common.utils.date.DateFormatUtil;
import com.huatu.tiku.constants.teacher.TeacherErrors;
import com.huatu.tiku.entity.knowledge.QuestionKnowledge;
import com.huatu.tiku.entity.question.PaperQuestionSimpleInfo;
import com.huatu.tiku.entity.question.QuestionSimpleInfo;
import com.huatu.tiku.entity.teacher.PaperAssembly;
import com.huatu.tiku.entity.teacher.PaperSearchInfo;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.enums.QuestionInfoEnum;
import com.huatu.tiku.enums.QuestionElementEnum;
import com.huatu.tiku.teacher.service.download.v1.WordWriteServiceV1;
import com.huatu.tiku.teacher.service.download.WordWriteService;
import com.huatu.tiku.teacher.service.knowledge.KnowledgeService;
import com.huatu.tiku.teacher.service.knowledge.QuestionKnowledgeService;
import com.huatu.tiku.teacher.service.paper.PaperAssemblyService;
import com.huatu.tiku.teacher.service.paper.PaperSearchService;
import com.huatu.tiku.teacher.util.file.CommonFileUtil;
import com.huatu.tiku.teacher.util.image.ImageUtil;
import com.huatu.tiku.util.file.FunFileUtils;
import com.huatu.tiku.util.file.UploadFileUtil;
import com.huatu.tiku.util.html.HtmlConvertUtil;
import com.huatu.ztk.commons.JsonUtil;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.rtf.RtfWriter2;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by huangqingpeng on 2018/8/15.
 */
@Slf4j
@Service
public class WordWriteServiceImpl implements WordWriteService {

    @Autowired
    PaperSearchService paperSearchService;

    @Autowired
    WordWriteServiceV1 wordWriteServiceV1;
    @Autowired
    KnowledgeService knowledgeService;
    @Autowired
    QuestionKnowledgeService questionKnowledgeService;
    @Autowired
    PaperAssemblyService paperAssemblyService;
    /**
     * word 文件名后缀
     */
    public final static String WORD_TAIL_NAME = ".doc";

    @Override
    public String download(long paperId, PaperInfoEnum.TypeInfo typeInfo, QuestionElementEnum.QuestionFieldEnum exportType) {
        String fileName = makeWordByPaper(paperId, typeInfo, exportType);
        String name = fileName.replaceAll(WORD_TAIL_NAME, "");
        //cdn上传下载文件后缀
        String tailName = "_" + DateFormatUtil.NUMBER_FORMAT.format(new Date()) + WORD_TAIL_NAME;
        File file = new File(FunFileUtils.TMP_WORD_SOURCE_FILEPATH + fileName);
        try {
            //cdn上传需要的文件名
            String tempName = new String(name.getBytes("UTF-8"), "iso-8859-1") + tailName;
            UploadFileUtil.getInstance().ftpUploadFile(file, tempName, FunFileUtils.WORD_FILE_SAVE_PATH);
            FunFileUtils.deleteFile(file);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            if (file.exists()) {
                FunFileUtils.deleteFile(file);
            }
        }
        //cdn文件下载路径
        return FunFileUtils.WORD_FILE_SAVE_URL + name + tailName;
    }

    @Override
    public String downLoadList(List<Long> paperIds, PaperInfoEnum.TypeInfo typeInfo, QuestionElementEnum.QuestionFieldEnum exportType) {
        List<String> fileNames = Lists.newArrayList();
        for (Long paperId : paperIds) {
            String fileName = makeWordByPaper(paperId, typeInfo, exportType);
            fileNames.add(fileName.replace(WORD_TAIL_NAME, ""));
        }
        //生成压缩包
        String zipName = DateFormatUtil.NUMBER_FORMAT.format(new Date());
        boolean bln = false;
        try {
            bln = FunFileUtils.zipFile(zipName, fileNames, WORD_TAIL_NAME,FunFileUtils.TMP_WORD_SOURCE_FILEPATH);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("获取压缩包：{}", FunFileUtils.TMP_WORD_SOURCE_FILEPATH + zipName + ".zip");
        File fileZip = new File(FunFileUtils.TMP_WORD_SOURCE_FILEPATH + zipName + ".zip");
        if (bln) {
            //ftp上传文件  ?  windows 系统 ，上次服务器放开注释
            try {
                UploadFileUtil.getInstance().ftpUploadFile(fileZip, new String(fileZip.getName().getBytes("UTF-8"), "iso-8859-1"), FunFileUtils.WORD_FILE_SAVE_PATH);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            //删除临时文件
            FunFileUtils.deleteFile(fileZip);
            //下载地址
            return FunFileUtils.WORD_FILE_SAVE_URL + zipName + ".zip";
        } else {
            //删除临时文件
            FunFileUtils.deleteFile(fileZip);
            throw new BizException(ErrorResult.create(1000107, "下载试卷失败"));
        }
    }


    /**
     * 通过paperId生成word文件，返回文件名
     *
     * @param paperId    试卷id
     * @param typeInfo   试卷类型
     * @param exportType 导出方式
     * @return word文件名
     */
    private String makeWordByPaper(long paperId, PaperInfoEnum.TypeInfo typeInfo, QuestionElementEnum.QuestionFieldEnum exportType) {
        PaperSearchInfo paperSearchInfo = null;
        PaperAssembly paperAssembly = null;
        String name = "";
        if (typeInfo.equals(PaperInfoEnum.TypeInfo.ENTITY)) {
            paperSearchInfo = paperSearchService.entityDetail(paperId);
            name = paperSearchInfo.getName();
        } else if (typeInfo.equals(PaperInfoEnum.TypeInfo.SIMULATION)) {
            paperSearchInfo = paperSearchService.entityActivityDetail(paperId);
            name = paperSearchInfo.getName();
        } else {
            paperAssembly = paperAssemblyService.detailWithQuestion(paperId);
            name = paperAssembly.getName();
        }
        if (paperSearchInfo == null && paperAssembly == null) {
            throw new BizException(TeacherErrors.NO_EXISTED_PAPER);
        }
        if (exportType.equals(QuestionElementEnum.QuestionFieldEnum.STEM)) {
            name = Joiner.on("_").join(name, "题干");
        } else if (exportType.equals(QuestionElementEnum.QuestionFieldEnum.ANSWER)) {
            name = Joiner.on("_").join(name, "答案");
        }
        //本地word写入文件
        File file = new File(FunFileUtils.TMP_WORD_SOURCE_FILEPATH + name + WORD_TAIL_NAME);
        Document document = null;
        RtfWriter2 rtfWriter2 = null;
        try {
            document = CommonFileUtil.newDocument();
            //正文试题字体
            Font font = CommonFileUtil.newFont(14, Font.NORMAL);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            rtfWriter2 = RtfWriter2.getInstance(document, fileOutputStream);
            document.open();
            //试卷名写入
            wordWriteServiceV1.addTitleElement(document, name);
            //真题卷和模拟卷部分数据写入word
            if (typeInfo.equals(PaperInfoEnum.TypeInfo.ENTITY) || typeInfo.equals(PaperInfoEnum.TypeInfo.SIMULATION)) {
                ImageUtil.save(new StringBuilder(JsonUtil.toJson(paperSearchInfo)));
                List<PaperSearchInfo.ModuleInfo> modules = paperSearchInfo.getModuleInfo();
                for (PaperSearchInfo.ModuleInfo module : modules) {
                    //模块写入
                    wordWriteServiceV1.addModuleElement(document, module.getName(), module.getId());
                    List<PaperQuestionSimpleInfo> questions = module.getList();
                    for (PaperQuestionSimpleInfo question : questions) {
                        addQuestionElement(document, font, question, exportType);
                    }
                }
            } else {      //手工组卷部分数据写入
                ImageUtil.save(new StringBuilder(JsonUtil.toJson(paperAssembly)));
                List<QuestionSimpleInfo> questionSimpleInfoList = paperAssembly.getQuestionSimpleInfoList();
                for (QuestionSimpleInfo questionSimpleInfo : questionSimpleInfoList) {
                    PaperQuestionSimpleInfo question = new PaperQuestionSimpleInfo();
                    BeanUtils.copyProperties(questionSimpleInfo, question);
                    /**
                     * 如果是复合类的试题，需要将子题的属性也做转化
                     */
                    Integer questionType = questionSimpleInfo.getQuestionType();
                    QuestionInfoEnum.QuestionSaveTypeEnum saveTypeByQuestionType = QuestionInfoEnum.getSaveTypeByQuestionType(questionType);
                    if(saveTypeByQuestionType.equals(QuestionInfoEnum.QuestionSaveTypeEnum.COMPOSITE)){
                        List<QuestionSimpleInfo> children = questionSimpleInfo.getChildren();
                        if(CollectionUtils.isNotEmpty(children)){
                            List<PaperQuestionSimpleInfo> paperQuestionSimpleInfos = Lists.newArrayList();
                            for (QuestionSimpleInfo child : children) {
                                PaperQuestionSimpleInfo paperQuestionSimpleInfo = new PaperQuestionSimpleInfo();
                                BeanUtils.copyProperties(child, paperQuestionSimpleInfo);
                                paperQuestionSimpleInfos.add(paperQuestionSimpleInfo);
                            }
                            question.setChildren(paperQuestionSimpleInfos);
                        }
                    }
                    addQuestionElement(document, font, question, exportType);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (document != null) {
                document.close();
            }
            if (rtfWriter2 != null) {
                rtfWriter2.close();
            }
            if (file.exists()) {
                file.deleteOnExit();
            }
        }
        return file.getName();
    }

    /**
     * 添加试题结点
     *
     * @param document
     * @param font
     * @param question
     * @param exportType
     * @throws Exception
     */
    private void addQuestionElement(Document document, Font font, PaperQuestionSimpleInfo question, QuestionElementEnum.QuestionFieldEnum exportType) throws Exception {
        //题型
        if (QuestionInfoEnum.getSaveTypeByQuestionType(question.getQuestionType()) == QuestionInfoEnum.QuestionSaveTypeEnum.COMPOSITE) {
            //复合题写入
            wordWriteServiceV1.addQuestionElement(document, font, transMapData(question), exportType);
            List<PaperQuestionSimpleInfo> children = question.getChildren();
            for (PaperQuestionSimpleInfo child : children) {
                //子题写入
                wordWriteServiceV1.addQuestionElement(document, font, transMapData(child), exportType);
            }
        } else {
            //单题写入
            wordWriteServiceV1.addQuestionElement(document, font, transMapData(question), exportType);
        }
    }

    /**
     * 将PaperQuestionSimpleInfo中有用字段转化存储到hashMap中
     *
     * @param question
     * @return
     */
    private Map<String, Object> transMapData(PaperQuestionSimpleInfo question) {
        HashMap<String, Object> questionMap = Maps.newHashMap();
        QuestionInfoEnum.QuestionSaveTypeEnum saveTypeEnum = QuestionInfoEnum.getSaveTypeByQuestionType(question.getQuestionType());
        QuestionElementEnum.QuestionOperateEnum questionOperateEnum = QuestionElementEnum.QuestionOperateEnum.create(saveTypeEnum);
        List<QuestionElementEnum.ElementEnum> value = questionOperateEnum.getValue();
        for (QuestionElementEnum.ElementEnum elementEnum : value) {
            switch (elementEnum) {
                case SORT:
                    questionMap.put(elementEnum.getKey(), question.getSort() == null ? -1 : question.getSort());
                    break;
                case STEM:
                    questionMap.put(elementEnum.getKey(), question.getStem());
                    break;
                case TYPE:
                    questionMap.put(elementEnum.getKey(), question.getQuestionType());
                    break;
                case ANSWER:
                    questionMap.put(elementEnum.getKey(), question.getAnswer());
                    break;
                case CHOICE:
                    questionMap.put(elementEnum.getKey(), HtmlConvertUtil.assertChoicesContent(question.getChoices()));
                    break;
                case ANALYSIS:
                    questionMap.put(elementEnum.getKey(), question.getAnalyze());
                    break;
                case EXTEND:
                    questionMap.put(elementEnum.getKey(), question.getExtend());
                    break;
                case SOURCE:
                    questionMap.put(elementEnum.getKey(), question.getSource());
                    break;
                case MATERIAL:
                    List<String> materialContent = question.getMaterialContent();
                    //材料通过"【资料】"字符串隔开
                    questionMap.put(elementEnum.getKey(), StringUtils.join(materialContent, elementEnum.getName()));
                    break;
                case KNOWLEDGE:
                    Example example = new Example(QuestionKnowledge.class);
                    example.and().andEqualTo("questionId", question.getId());
                    List<QuestionKnowledge> questionKnowledges = questionKnowledgeService.selectByExample(example);
                    if (CollectionUtils.isNotEmpty(questionKnowledges)) {
                        List<String> names = knowledgeService.getKnowledgeNameByIds(questionKnowledges.stream().map(i -> i.getKnowledgeId()).collect(Collectors.toList()));
                        questionMap.put(elementEnum.getKey(), StringUtils.join(names, ","));
                    }
                    break;
                case QUESTION_ID:
                    questionMap.put(elementEnum.getKey(), question.getId());
                    break;
                case ANSWER_COMMENT:
                    questionMap.put(elementEnum.getKey(), question.getAnswer());
                    break;
            }
        }
        return questionMap;
    }


}
