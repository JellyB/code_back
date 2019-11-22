package com.huatu.tiku.essay.service.impl;


import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.Export.ExportKeyWordConstant;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.constant.status.EssayAnswerKeyWordConstant;
import com.huatu.tiku.essay.constant.status.EssayMaterialConstant;
import com.huatu.tiku.essay.constant.status.EssayQuestionConstant;
import com.huatu.tiku.essay.constant.status.QuestionCorrectTypeConstant;
import com.huatu.tiku.essay.entity.*;
import com.huatu.tiku.essay.essayEnum.EssayQuestionTypeEnum;
import com.huatu.tiku.essay.essayEnum.EssaySubScoreRule;
import com.huatu.tiku.essay.repository.*;
import com.huatu.tiku.essay.service.EssayExportService;
import com.huatu.tiku.essay.service.EssayKeyWordExportService;
import com.huatu.tiku.essay.service.EssayRuleService;
import com.huatu.tiku.essay.util.file.*;
import com.huatu.tiku.essay.vo.admin.*;
import com.huatu.tiku.essay.vo.export.EssayExportPaperVO;
import com.huatu.tiku.essay.vo.export.EssayExportQuestionVO;
import com.huatu.tiku.essay.vo.export.EssayExportReqVO;
import com.huatu.tiku.essay.vo.resp.EssayQuestionDeductRuleVO;
import com.huatu.tiku.essay.vo.resp.EssayStandardAnswerRuleSpecialStripVO;
import com.huatu.tiku.essay.vo.resp.EssayStandardAnswerRuleVO;
import com.huatu.tiku.essay.web.controller.admin.EssayRuleController;
import com.huatu.ztk.commons.JsonUtil;
import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.rtf.RtfWriter2;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.huatu.tiku.essay.constant.status.EssayAnswerKeyWordConstant.QUESTION_PARENT_KEYWORD_WITHOUT_DESC;
import static com.huatu.tiku.essay.util.file.FunFileUtils.ESSAY_FILE_SAVE_PATH;
import static com.huatu.tiku.essay.util.file.FunFileUtils.PDF_ESSAY_URL;
import static com.huatu.tiku.essay.util.file.HtmlFileUtil.deleteFile;
import static com.huatu.tiku.essay.vo.export.EssayExportReqVO.ANSWER_WITH_CORRECTED;

@Service
@Slf4j
public class EssayExportServiceImpl implements EssayExportService {
    @Autowired
    EssayQuestionAnswerRepository essayQuestionAnswerRepository;
    @Autowired
    private EssayMaterialRepository essayMaterialRepository;
    @Autowired
    private EssayQuestionBaseRepository essayQuestionBaseRepository;
    @Autowired
    private EssayQuestionDetailRepository essayQuestionDetailRepository;
    @Autowired
    private EssayStandardAnswerRepository essayStandardAnswerRepository;
    @Autowired
    private EssayPaperBaseRepository essayPaperBaseRepository;
    @Autowired
    private UploadFileUtil uploadFileUtil;

    @Autowired
    private EssayStandardAnswerKeyPhraseRepository essayStandardAnswerKeyPhraseRepository;

    @Autowired
    private EssayRuleService essayRuleService;

    @Autowired
    private EssayRuleController essayRuleController;

    @Autowired
    EssayKeyWordExportService essayKeyWordExportService;


    @Value("${pdf.essay}")
    private String pdfEssay;
    @Value("${pdf.temporary}")
    private String filePath;

    @Value("${pdf.logo}")
    private String oldLogo;
    @Value("${pdf.new.logo}")
    private String newLogo;
    @Value("${pdf.slogan}")
    private String slogan;
    @Value("${pdf.vhuatu}")
    private String vhuatu;

    /**
     * 生成pdf
     *
     * @param vo
     */
    @Override
    public String createFile(EssayExportReqVO vo) {
        String cdnPath = "";
        int fileType = vo.getFileType();
        //先根据paperID封装试卷对象
        List<EssayExportPaperVO> paperVOList = findPaperVOList(vo);

        /**
         * 暂时只支持单个试卷的处理   old
         if(CollectionUtils.isNotEmpty(paperVOList) && paperVOList.get(0) != null){
         if(fileType == EssayExportReqVO.FILE_TYPE_PDF){
         cdnPath = producePdf(paperVOList.get(0),vo.getType());
         }else{
         cdnPath =produceDoc(paperVOList.get(0),vo.getType());
         }
         */
        if (paperVOList.size() == 1) {
            if (fileType == EssayExportReqVO.FILE_TYPE_PDF) {
                cdnPath = producePdf(paperVOList.get(0), vo.getType());
            } else {
                cdnPath = produceDoc(paperVOList.get(0), vo.getType());

            }
        } else {
            if (fileType == EssayExportReqVO.FILE_TYPE_PDF) {
                cdnPath = producePdf(paperVOList, vo.getType());
            } else {
                cdnPath = produceDoc(paperVOList, vo.getType());
            }
        }

        return cdnPath;

    }

    @Override
    public String exportAnswer(EssayExportReqVO vo) {
        List<Long> answerIdList = vo.getAnswerIdList();
        List<EssayQuestionAnswer> questionAnswerList = essayQuestionAnswerRepository.findByIdIn(answerIdList);
        if (CollectionUtils.isEmpty(questionAnswerList)) {
            log.info("所选答题卡列表为空");
            throw new BizException(EssayErrors.ANSWER_LIST_EMPTY);
        }

        long questionDetailId = questionAnswerList.get(0).getQuestionDetailId();

        EssayQuestionDetail questionDetail = essayQuestionDetailRepository.findOne(questionDetailId);
        String stem = questionDetail.getStem();
        if (vo.getFileType() == EssayExportReqVO.FILE_TYPE_WORD) {
            return produceAnswerDoc(vo, stem, questionAnswerList);

        } else if (vo.getFileType() == EssayExportReqVO.FILE_TYPE_PDF) {
            return produceAnswerPdf(vo, stem, questionAnswerList);
        }
        return "";
    }

    private String produceAnswerPdf(EssayExportReqVO vo, String stem, List<EssayQuestionAnswer> questionAnswerList) {
        com.itextpdf.text.Document document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4, 50, 50, 100, 55);
        String randomStr = UUID.randomUUID().toString().replaceAll("-", "");
        String newPath = filePath + stem + randomStr + ".pdf";

        try {
            PdfUtil pdf = new PdfUtil(newPath, document);
            document.open();
            //录入标题
            pdf.addTitle(stem, document);


            //遍历试题
            for (EssayQuestionAnswer questionAnswer : questionAnswerList) {
                pdf.addBaseContent(contentFilter(getAnswerDesc(questionAnswer).replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"), EssayExportReqVO.FILE_TYPE_PDF), document);

                String content = (StringUtils.isEmpty(questionAnswer.getContent()) ? "" : questionAnswer.getContent());
                content = content + "\n\n";
                pdf.addBaseTitle("【样本原样】", document);
                pdf.addBaseContent(contentFilter(content, EssayExportReqVO.FILE_TYPE_PDF), document);

                if (vo.getType() == ANSWER_WITH_CORRECTED) {
                    pdf.addBaseTitle("【批改后样本】", document);
                    pdf.correct(questionAnswer.getCorrectedContent(), document, questionAnswer.getQuestionType(), questionAnswer.getId());
                }

            }
            document.close();
            newPath = pdf.addWaterImage(newPath, newLogo, slogan, vhuatu, oldLogo);

        } catch (Exception e) {
            e.printStackTrace();
        }
        String finalPath = upload(newPath);
        return finalPath;
    }

    private String produceAnswerDoc(EssayExportReqVO vo, String stem, List<EssayQuestionAnswer> questionAnswerList) {

        String randomStr = UUID.randomUUID().toString().replaceAll("-", "");
        String newPath = stem + randomStr + ".doc";

        // 设置纸张大小
        Document document = null;
        RtfWriter2 writer = null;
        try {
            // 设置纸张大小
            document = new Document(PageSize.A4);

            File file = new File(newPath);

            writer = RtfWriter2.getInstance(document, new FileOutputStream(file));
            // 设置中文字体
            BaseFont bfChinese = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            // 标题字体风格
            Font titleFont = new Font(bfChinese, 16, Font.BOLD);
            //二级标题标题字体风格
            Font subTitleFont = new Font(bfChinese, 14, Font.BOLD);

            // 正文字体风格
            Font contentFont = new Font(bfChinese, 10, Font.NORMAL);
            document.open();
            //录入标题
            addContent(stem, document, titleFont, Element.ALIGN_CENTER);
            for (EssayQuestionAnswer questionAnswer : questionAnswerList) {
                String content = (StringUtils.isEmpty(questionAnswer.getContent()) ? "" : questionAnswer.getContent());
                content = content + "\n\n";
                addContent(getAnswerDesc(questionAnswer), document, contentFont, Element.ALIGN_CENTER);
                addContent("【样本原样】", document, subTitleFont, Element.ALIGN_LEFT);
                addContent(content, document, contentFont, Element.ALIGN_LEFT);

                //批改后的
                if (vo.getType() == ANSWER_WITH_CORRECTED) {
                    addContent("【批改后样本】", document, subTitleFont, Element.ALIGN_LEFT);
                    String correctedContent = (StringUtils.isEmpty(questionAnswer.getCorrectedContent()) ? "" : questionAnswer.getCorrectedContent());
                    correctedContent = correctedContent + "\n\n";
                    DocUtil.correct(correctedContent, document);
                }
            }

            //关闭文档
            document.close();
            //关闭书写器
            writer.close();
        } catch (Exception e) {
            //关闭文档
            document.close();
            //关闭书写器
            if (writer != null) {
                writer.close();
            }
            e.printStackTrace();
        }
        return upload(newPath);

    }


    /**
     * 生成指定试卷pdf
     *
     * @param paperVOList
     */
    private String producePdf(List<EssayExportPaperVO> paperVOList, int type) {

        String randomStr = UUID.randomUUID().toString().replaceAll("-", "");
        String path = filePath + randomStr;
        File directoryFile = new File(path);
        if (!directoryFile.exists()) {
            directoryFile.mkdir();
        }
        for (EssayExportPaperVO paperVO : paperVOList) {
            String newPath = path + "/" + UUID.randomUUID().toString().replaceAll("-", "") + ".pdf";
            log.info("pdfPath：" + newPath);

            try {
                com.itextpdf.text.Document document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4, 50, 50, 100, 55);
                PdfUtil pdf = new PdfUtil(newPath, document);
                document.open();

                //录入标题
                pdf.addTitle(paperVO.getPaperName(), document);
                //录入材料
                pdf.addBaseTitle("【给定资料】", document);

                List<EssayMaterial> materialList = paperVO.getMaterialList();
                for (EssayMaterial material : materialList) {
                    pdf.addBaseContent(contentFilter(material.getContent(), EssayExportReqVO.FILE_TYPE_PDF), document);
                }
                //遍历试题
                List<EssayExportQuestionVO> questionList = paperVO.getQuestionList();
                for (EssayExportQuestionVO questionVO : questionList) {
                    String stem = contentFilter(questionVO.getStem(), EssayExportReqVO.FILE_TYPE_PDF);
                    List<EssayStandardAnswer> answerList = questionVO.getAnswerList();
                    String analysis = contentFilter(questionVO.getAnalysis(), EssayExportReqVO.FILE_TYPE_PDF);
                    String review = contentFilter(questionVO.getReview(), EssayExportReqVO.FILE_TYPE_PDF);
                    pdf.addBaseTitle("【作答要求】", document);
                    pdf.addBaseContent(stem, document);
                    if (type == EssayExportReqVO.BASE_CONTENT_WITH_ANSWER || type == EssayExportReqVO.BASE_CONTENT_WITH_ANSWER_AND_ANALYSIS) {
                        if (answerList.get(0).getAnswerFlag() == 0) {
                            pdf.addBaseTitle("【参考答案】", document);
                        } else {
                            pdf.addBaseTitle("【标准答案】", document);
                        }
                        if (StringUtils.isNotEmpty(answerList.get(0).getTopic())) {
                            com.itextpdf.text.Paragraph paragraph1 = new com.itextpdf.text.Paragraph(answerList.get(0).getTopic(), pdf.CONTENT_FONT);
                            paragraph1.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                            document.add(paragraph1);
                        }
                        if (StringUtils.isNotEmpty(answerList.get(0).getSubTopic())) {
                            com.itextpdf.text.Paragraph paragraph2 = new com.itextpdf.text.Paragraph(answerList.get(0).getSubTopic(), pdf.CONTENT_FONT);
                            paragraph2.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                            document.add(paragraph2);
                        }
                        if (StringUtils.isNotEmpty(answerList.get(0).getCallName())) {
                            com.itextpdf.text.Paragraph paragraph3 = new com.itextpdf.text.Paragraph(answerList.get(0).getCallName(), pdf.CONTENT_FONT);
                            paragraph3.setIndentationLeft(5);
                            document.add(paragraph3);
                        }
                        //添加答案
                        pdf.addBaseContent(contentFilter(answerList.get(0).getAnswerComment(), EssayExportReqVO.FILE_TYPE_PDF), document);
                        String inscribedName = answerList.get(0).getInscribedName();
                        if (StringUtils.isNotEmpty(inscribedName)) {
                            String[] split = inscribedName.split("<br/>");
                            for (int i = 0; i < split.length; i++) {
                                com.itextpdf.text.Paragraph paragraph5 = new com.itextpdf.text.Paragraph(split[i], pdf.CONTENT_FONT);
                                paragraph5.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
                                document.add(paragraph5);
                            }
                        }
                        if (StringUtils.isNotEmpty(answerList.get(0).getInscribedDate())) {
                            com.itextpdf.text.Paragraph paragraph4 = new com.itextpdf.text.Paragraph(answerList.get(0).getInscribedDate(), pdf.CONTENT_FONT);
                            paragraph4.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
                            document.add(paragraph4);
                        }

                    }
                    if (type == EssayExportReqVO.BASE_CONTENT_WITH_ANSWER_AND_ANALYSIS) {
                        pdf.addBaseTitle("【解析】", document);
                        pdf.addBaseContent(getDifficultGradeStr(questionVO.getDifficultGrade()), document);
                        pdf.addBaseContent(analysis, document);
                        pdf.addBaseTitle("【经验小结】", document);
                        pdf.addBaseContent(review, document);
                    }
                }
                document.close();
                String newUrl = pdf.addWaterImage(newPath, newLogo, slogan, vhuatu, oldLogo);
                pdf.addWaterImageForZip(newUrl, path + "/" + paperVO.getPaperName() + ".pdf", newLogo, slogan, vhuatu, oldLogo);

                //删除旧文件
                File file = new File(newPath);
                if (file.isFile() && file.exists()) {
                    file.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            String str = "申论试题" + DateFormatUtils.format(new Date(), "yyyyMMdd") + ExportTypeEnum.create(type).getTitle();
            String pat = filePath + str + randomStr + ".zip";
            ZipUtils.doCompress(path, pat);
            String finalPath = upload(pat);
            return finalPath;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }


    /**
     * 生成指定试卷pdf
     *
     * @param paperVO
     */
    private String producePdf(EssayExportPaperVO paperVO, int type) {
        com.itextpdf.text.Document document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4, 50, 50, 100, 55);
        String randomStr = UUID.randomUUID().toString().replaceAll("-", "");
        String newPath = filePath + paperVO.getPaperName() + randomStr + ".pdf";

        try {
            PdfUtil pdf = new PdfUtil(newPath, document);
            document.open();

            //录入标题
            pdf.addTitle(paperVO.getPaperName(), document);
            //录入材料
            pdf.addBaseTitle("【给定资料】", document);

            List<EssayMaterial> materialList = paperVO.getMaterialList();
            for (EssayMaterial material : materialList) {
                pdf.addBaseContent(contentFilter(material.getContent(), EssayExportReqVO.FILE_TYPE_PDF), document);
            }
            //遍历试题
            List<EssayExportQuestionVO> questionList = paperVO.getQuestionList();
            for (EssayExportQuestionVO questionVO : questionList) {

                String stem = contentFilter(questionVO.getStem(), EssayExportReqVO.FILE_TYPE_PDF);
                List<EssayStandardAnswer> answerList = questionVO.getAnswerList();
                String analysis = contentFilter(questionVO.getAnalysis(), EssayExportReqVO.FILE_TYPE_PDF);
                String review = contentFilter(questionVO.getReview(), EssayExportReqVO.FILE_TYPE_PDF);
                pdf.addBaseTitle("【作答要求】", document);
                pdf.addBaseContent(stem, document);

                if (type == EssayExportReqVO.BASE_CONTENT_WITH_ANSWER || type == EssayExportReqVO.BASE_CONTENT_WITH_ANSWER_AND_ANALYSIS) {
                    if (answerList.get(0).getAnswerFlag() == 0) {
                        pdf.addBaseTitle("【参考答案】", document);
                    } else {
                        pdf.addBaseTitle("【标准答案】", document);
                    }
                    if (StringUtils.isNotEmpty(answerList.get(0).getTopic())) {
                        com.itextpdf.text.Paragraph paragraph1 = new com.itextpdf.text.Paragraph(answerList.get(0).getTopic(), pdf.CONTENT_FONT);
                        paragraph1.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                        document.add(paragraph1);
                    }
                    if (StringUtils.isNotEmpty(answerList.get(0).getSubTopic())) {
                        com.itextpdf.text.Paragraph paragraph2 = new com.itextpdf.text.Paragraph(answerList.get(0).getSubTopic(), pdf.CONTENT_FONT);
                        paragraph2.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                        document.add(paragraph2);
                    }
                    if (StringUtils.isNotEmpty(answerList.get(0).getCallName())) {
                        com.itextpdf.text.Paragraph paragraph3 = new com.itextpdf.text.Paragraph(answerList.get(0).getCallName(), pdf.CONTENT_FONT);
                        paragraph3.setIndentationLeft(5);
                        document.add(paragraph3);
                    }
                    //添加答案
                    pdf.addBaseContent(contentFilter(answerList.get(0).getAnswerComment(), EssayExportReqVO.FILE_TYPE_PDF), document);


                    String inscribedName = answerList.get(0).getInscribedName();
                    if (StringUtils.isNotEmpty(inscribedName)) {
                        String[] split = inscribedName.split("<br/>");
                        for (int i = 0; i < split.length; i++) {
                            com.itextpdf.text.Paragraph paragraph5 = new com.itextpdf.text.Paragraph(split[i], pdf.CONTENT_FONT);
                            paragraph5.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
                            document.add(paragraph5);
                        }
                    }
                    if (StringUtils.isNotEmpty(answerList.get(0).getInscribedDate())) {
                        com.itextpdf.text.Paragraph paragraph4 = new com.itextpdf.text.Paragraph(answerList.get(0).getInscribedDate(), pdf.CONTENT_FONT);
                        paragraph4.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
                        document.add(paragraph4);
                    }


                }

                if (type == EssayExportReqVO.BASE_CONTENT_WITH_ANSWER_AND_ANALYSIS) {
                    pdf.addBaseTitle("【解析】", document);
                    pdf.addBaseContent(getDifficultGradeStr(questionVO.getDifficultGrade()), document);
                    pdf.addBaseContent(analysis, document);

                    pdf.addBaseTitle("【经验小结】", document);
                    pdf.addBaseContent(review, document);

                }
            }

            document.close();
            newPath = pdf.addWaterImage(newPath, newLogo, slogan, vhuatu, oldLogo);

        } catch (Exception e) {
            e.printStackTrace();
        }
        String finalPath = upload(newPath);
        return finalPath;

    }

    private String upload(String newPath) {
        File file = new File(newPath);
        String fileName = getFileName(newPath);
        //向文件中写数据
        try {
            uploadFileUtil.ftpUploadFile(file, new String(fileName.getBytes("UTF-8"), "iso-8859-1"), ESSAY_FILE_SAVE_PATH);
        } catch (BizException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String finalPath = PDF_ESSAY_URL + fileName;
        deleteFile(newPath);
        return finalPath;
    }

    /**
     * 截取文件路径
     *
     * @param filePath
     * @return
     */
    private String getFileName(String filePath) {
        int i = filePath.lastIndexOf("/");
        String fileName = filePath.substring(i + 1, filePath.length());
        log.info(fileName);
        return fileName;
    }

    /**
     * 生成指定试卷doc
     *
     * @param paperVO
     */
    private String produceDoc(EssayExportPaperVO paperVO, int type) {
        boolean flag = false;
        String randomStr = UUID.randomUUID().toString().replaceAll("-", "");
        String newPath = filePath + paperVO.getPaperName() + randomStr + ".doc";

        // 设置纸张大小
        Document document = null;
        RtfWriter2 writer = null;
        String paperName = paperVO.getPaperName();
        try {
            // 设置纸张大小
            document = new Document(PageSize.A4);

            File file = new File(newPath);

            writer = RtfWriter2.getInstance(document, new FileOutputStream(file));
            // 设置中文字体
            BaseFont bfChinese = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            // 标题字体风格
            Font titleFont = new Font(bfChinese, 16, Font.BOLD);
            //二级标题标题字体风格
            Font subTitleFont = new Font(bfChinese, 14, Font.BOLD);

            Font littleTitleFont = new Font(bfChinese, 10, Font.BOLD);

            // 正文字体风格
            Font contentFont = new Font(bfChinese, 10, Font.NORMAL);
            document.open();
            //录入标题
            addContent(paperName, document, titleFont, Element.ALIGN_CENTER);

            //录入材料
            addContent("【给定资料】", document, subTitleFont, Element.ALIGN_LEFT);

            List<EssayMaterial> materialList = paperVO.getMaterialList();
            for (EssayMaterial material : materialList) {
                addContent(material.getContent(), document, contentFont, Element.ALIGN_LEFT);
            }
            //遍历试题
            List<EssayExportQuestionVO> questionList = paperVO.getQuestionList();
            for (EssayExportQuestionVO questionVO : questionList) {

                String stem = questionVO.getStem();

                List<EssayStandardAnswer> answerList = questionVO.getAnswerList();
                String analysis = questionVO.getAnalysis();
                String review = questionVO.getReview();
                addContent("【作答要求】", document, subTitleFont, Element.ALIGN_LEFT);
                addContent(stem, document, contentFont, Element.ALIGN_LEFT);

                if (type == EssayExportReqVO.BASE_CONTENT_WITH_ANSWER || type == EssayExportReqVO.BASE_CONTENT_WITH_ANSWER_AND_ANALYSIS
                        || type == EssayExportReqVO.BASE_CONTENT_WITH_ANSWER_AND_ANALYSIS_ARITHMETIC) {
                    if (answerList.get(0).getAnswerFlag() == 0) {
                        addContent("【参考答案】", document, subTitleFont, Element.ALIGN_LEFT);
                    } else {
                        addContent("【标准答案】", document, subTitleFont, Element.ALIGN_LEFT);
                    }
                    addContent(answerList.get(0).getTopic(), document, contentFont, Element.ALIGN_CENTER);
                    addContent(answerList.get(0).getSubTopic(), document, contentFont, Element.ALIGN_CENTER);
                    addContent(answerList.get(0).getCallName(), document, contentFont, Element.ALIGN_LEFT);
                    addContent(answerList.get(0).getAnswerComment(), document, contentFont, Element.ALIGN_LEFT);

                    String inscribedName = answerList.get(0).getInscribedName();
                    if (StringUtils.isNotEmpty(inscribedName)) {
                        String[] split = inscribedName.split("<br/>");
                        for (int i = 0; i < split.length; i++) {
                            addContent(split[i], document, contentFont, Element.ALIGN_RIGHT);
                        }
                    }
                    addContent(answerList.get(0).getInscribedDate(), document, contentFont, Element.ALIGN_RIGHT);
                }

                if (type == EssayExportReqVO.BASE_CONTENT_WITH_ANSWER_AND_ANALYSIS || type == EssayExportReqVO.BASE_CONTENT_WITH_ANSWER_AND_ANALYSIS_ARITHMETIC) {
                    addContent("【解析】", document, subTitleFont, Element.ALIGN_LEFT);
                    addContent(getDifficultGradeStr(questionVO.getDifficultGrade()), document, contentFont, Element.ALIGN_LEFT);
                    addContent(analysis, document, contentFont, Element.ALIGN_LEFT);

                    addContent("【经验小结】", document, subTitleFont, Element.ALIGN_LEFT);
                    addContent(review, document, contentFont, Element.ALIGN_LEFT);
                }

                //算法导出
                if (type == EssayExportReqVO.BASE_CONTENT_WITH_ANSWER_AND_ANALYSIS_ARITHMETIC) {

                    addContent(ExportKeyWordConstant.adminArithmeticTitle, document, subTitleFont, Element.ALIGN_LEFT);
                    //关键词关键字
                    writeArithmetic(document, contentFont, littleTitleFont, questionVO);
                    //应用文议论文格式
                    if (questionVO.getQuestionType() == EssayQuestionTypeEnum.YLWX.getValue() || questionVO.getQuestionType() == EssayQuestionTypeEnum.YYXZ.getValue()) {
                        AdminQuestionFormatVO questionFormatVO = questionVO.getQuestionFormatVO();
                        if (null != questionFormatVO) {
                            essayKeyWordExportService.writeAnswerFormat(questionFormatVO, document, contentFont);
                        }
                    }
                    //扣分项
                    if (null != questionVO.getQuestionKeyRuleVO()) {
                        //log.info("扣分项内容是:{}", questionVO.getQuestionKeyRuleVO());
                        EssayQuestionDeductRuleVO deductRuleVO = questionVO.getDeductRuleVO();
                        if (null != deductRuleVO) {
                            // 普通扣分规则
                            List<EssayStandardAnswerRuleVO> commonRuleList = deductRuleVO.getCommonRuleList();
                            if (CollectionUtils.isNotEmpty(commonRuleList)) {
                                for (EssayStandardAnswerRuleVO answerRuleVO : commonRuleList) {
                                    //字数限制
                                    if (answerRuleVO.getType() == EssaySubScoreRule.SubScoreEnum.wordLimit.getCode()) {
                                        String wordLimit = String.format(ExportKeyWordConstant.wordLimit, answerRuleVO.getMinNum(), answerRuleVO.getMaxNum(), answerRuleVO.getDeductTypeNum());
                                        if (StringUtils.isNotEmpty(wordLimit)) {
                                            addContent(ExportKeyWordConstant.wordLimitTitle, document, littleTitleFont, Element.ALIGN_LEFT);
                                            addContent(wordLimit, document, contentFont, Element.ALIGN_LEFT);
                                        }
                                    }
                                    //句子冗长
                                    if (answerRuleVO.getType() == EssaySubScoreRule.SubScoreEnum.sentenceTooLong.getCode()) {
                                        //扣分方式
                                        String sentence = String.format(ExportKeyWordConstant.wordTooLong, answerRuleVO.getMaxNum(),
                                                EssaySubScoreRule.getSubScoreType(answerRuleVO.getDeductType()).getContent(), answerRuleVO.getDeductTypeScorePercent());
                                        if (StringUtils.isNotEmpty(sentence)) {
                                            addContent(sentence, document, contentFont, Element.ALIGN_LEFT);
                                        }
                                    }
                                    //分段规则
                                    if (answerRuleVO.getType() == EssaySubScoreRule.SubScoreEnum.subsection_rules.getCode()) {
                                        String subSection = String.format(ExportKeyWordConstant.subsection_rules, answerRuleVO.getMinNum(), answerRuleVO.getMaxNum(),
                                                EssaySubScoreRule.getSubScoreType(answerRuleVO.getDeductType()).getContent(), answerRuleVO.getDeductTypeScorePercent());
                                        if (StringUtils.isNotEmpty(subSection)) {
                                            addContent(subSection, document, contentFont, Element.ALIGN_LEFT);
                                        }
                                    }
                                    //普通分条(分条扣分)
                                    if (answerRuleVO.getType() == EssaySubScoreRule.SubScoreEnum.common_not_subsection.getCode() ||
                                            answerRuleVO.getType() == EssaySubScoreRule.SubScoreEnum.common_not_subsection.getCode()) {
                                        String commonNotSubsection = String.format(ExportKeyWordConstant.common_not_subsection,
                                                EssaySubScoreRule.getSubScoreType(answerRuleVO.getDeductType()).getContent(), answerRuleVO.getDeductTypeScorePercent());
                                        if (StringUtils.isNotEmpty(commonNotSubsection)) {
                                            addContent(commonNotSubsection, document, contentFont, Element.ALIGN_LEFT);
                                        }
                                    }
                                    //普通分条(不出现扣分)
                                    if (answerRuleVO.getType() == EssaySubScoreRule.SubScoreEnum.common_subsection.getCode() ||
                                            answerRuleVO.getType() == EssaySubScoreRule.SubScoreEnum.common_not_subsection.getCode()) {
                                        String commonSubsection = String.format(ExportKeyWordConstant.common_subsection,
                                                EssaySubScoreRule.getSubScoreType(answerRuleVO.getDeductType()).getContent(), answerRuleVO.getDeductTypeScorePercent());
                                        if (StringUtils.isNotEmpty(commonSubsection)) {
                                            addContent(commonSubsection, document, contentFont, Element.ALIGN_LEFT);
                                        }
                                    }
                                }
                            }
                            //特殊表达规则
                            List<EssayStandardAnswerRuleSpecialStripVO> specialStripList = deductRuleVO.getSpecialStripList();
                            if (CollectionUtils.isNotEmpty(specialStripList)) {
                                for (EssayStandardAnswerRuleSpecialStripVO specialStripVO : specialStripList) {
                                    //不出现扣分
                                    if (specialStripVO.getType() == EssaySubScoreRule.SubScoreEnum.special_expression_not_exist.getCode()) {
                                        //表达形式
                                        String ruleSpecialSplit = getRuleSpecialSpilt.apply(specialStripVO);
                                        String specialExpression = String.format(ExportKeyWordConstant.special_expression_not_exist, ruleSpecialSplit,
                                                EssaySubScoreRule.getSubScoreType(specialStripVO.getDeductType()).getContent(), specialStripVO.getDeductScore());
                                        addContent(specialExpression, document, contentFont, Element.ALIGN_LEFT);
                                    }
                                    //出现扣分
                                    if (specialStripVO.getType() == EssaySubScoreRule.SubScoreEnum.special_expression_exist.getCode()) {
                                        //表达形式
                                        String ruleSpecialSplit = getRuleSpecialSpilt.apply(specialStripVO);
                                        String specialExpression = String.format(ExportKeyWordConstant.special_expression_exist, ruleSpecialSplit,
                                                EssaySubScoreRule.getSubScoreType(specialStripVO.getDeductType()).getContent(), specialStripVO.getDeductScore());
                                        addContent(specialExpression, document, contentFont, Element.ALIGN_LEFT);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            //关闭文档
            document.close();
            //关闭书写器
            writer.close();
        } catch (Exception e) {
            //关闭文档
            document.close();
            //关闭书写器
            if (writer != null) {
                writer.close();
            }
            e.printStackTrace();
        }
        return upload(newPath);
    }


    Function<EssayStandardAnswerRuleSpecialStripVO, String> getRuleSpecialSpilt = (ruleSpecialStripVO -> {
        if (null != ruleSpecialStripVO) {
            List<String> list = new ArrayList<>();
            list.add(ruleSpecialStripVO.getFirstWord());
            list.add(ruleSpecialStripVO.getSecondWord());
            list.add(ruleSpecialStripVO.getThirdWord());
            list.add(ruleSpecialStripVO.getFourthWord());
            list.add(ruleSpecialStripVO.getFifthWord());
            String collect = list.stream().filter(rule -> StringUtils.isNotEmpty(rule)).collect(Collectors.joining(","));
            return collect;
        }
        return "";
    });


    public void writeArithmetic(Document document, Font contentFont, Font littleTitleFont, EssayExportQuestionVO questionVO) throws DocumentException {

        if (null == questionVO || null == questionVO.getQuestionKeyRuleVO()) {
            return;
        }
        AdminQuestionKeyRuleVO questionKeyRuleVO = questionVO.getQuestionKeyRuleVO();
       // addContent(ExportKeyWordConstant.adminArithmeticTitle, document, littleTitleFont, Element.ALIGN_LEFT);
        log.info("类型是:{},算法内容是:{}", questionVO.getCorrectType(), JsonUtil.toJson(questionKeyRuleVO));

        Consumer<List<String>> writeKeyWordConsumer = (keyWordList -> {
            if (CollectionUtils.isNotEmpty(keyWordList)) {
                for (String keyWord : keyWordList) {
                    try {
                        Pattern pattern = Pattern.compile(".*?\\（[0-9]分");
                        Matcher matcher = pattern.matcher(keyWord);
                        String front = "";
                        String end = "";
                        while (matcher.find()) {
                            front = keyWord.substring(0, matcher.end());
                            end = keyWord.substring(matcher.end(), keyWord.length());
                        }
                        addKeyWordContent("    " + front, end, document, littleTitleFont, contentFont, Element.ALIGN_LEFT);
                    } catch (DocumentException e) {
                        e.printStackTrace();
                        log.info("关键词写入失败:{}");
                    }
                }
            }
        });
        //关键词
        if (questionVO.getCorrectType() == QuestionCorrectTypeConstant.KEY_WORD_CORRECT) {
            //1,带描述的关键词
            List<AdminQuestionKeyWordWithDescVO> keyWordWithDescVOList = questionKeyRuleVO.getKeyWordWithDescList();
            if (CollectionUtils.isNotEmpty(keyWordWithDescVOList)) {
                List<KeyWordExportFormateVo> keyWordExportFormatVos = essayKeyWordExportService.convertQuestionKeyWord(keyWordWithDescVOList);
                if (CollectionUtils.isNotEmpty(keyWordExportFormatVos)) {
                    for (KeyWordExportFormateVo keyWordExportFormateVo : keyWordExportFormatVos) {
                        //描述（如果有,则写入,如果没有描述，不写）
                        if (StringUtils.isNotEmpty(keyWordExportFormateVo.getPhrase())) {
                            addContent(keyWordExportFormateVo.getPhrase(), document, contentFont, Element.ALIGN_LEFT);
                        }
                        //关键词
                        writeKeyWordConsumer.accept(keyWordExportFormateVo.getKeyWordList());
                    }
                }
            }
            //2,只有关键词
            List<AdminQuestionKeyWordVO> keyWordList = questionKeyRuleVO.getKeyWordList();
            if (CollectionUtils.isNotEmpty(keyWordList)) {
                if (keyWordList.get(0).getId() > 0) {
                    writeKeyWordConsumer.accept(essayKeyWordExportService.getKeyWordContent(keyWordList));
                }
            }
        } else if (questionVO.getCorrectType() == QuestionCorrectTypeConstant.KEY_PHRASE_CORRECT) {
            //3.带描述关键句
            List<AdminQuestionKeyPhraseWithDescVO> keySentencesWithDescVoList = questionKeyRuleVO.getKeyPhraseWithDescList();
            if (CollectionUtils.isNotEmpty(keySentencesWithDescVoList)) {
                if (keySentencesWithDescVoList.get(0).getId() > 0) {
                    essayKeyWordExportService.writeKeySentenceWithDesc(keySentencesWithDescVoList, document, contentFont, Element.ALIGN_LEFT);
                }
            }
            //4.关键句
            List<AdminQuestionKeyPhraseVO> keyPhraseList = questionKeyRuleVO.getKeyPhraseList();
            if (CollectionUtils.isNotEmpty(keyPhraseList)) {
                if (keyPhraseList.get(0).getId() > 0) {
                    essayKeyWordExportService.writeKeySentence(keyPhraseList, document, littleTitleFont, contentFont);
                }
            }

        } else if (questionVO.getCorrectType() == 0) {
            //如果是议论文，导出主题，导出中心论点
            if (questionVO.getQuestionType() == EssayQuestionTypeEnum.YLWX.getValue()) {
                essayKeyWordExportService.writeTopicAndArgument(questionKeyRuleVO, document, littleTitleFont, contentFont, Element.ALIGN_LEFT);
            }
        }

    }


    /**
     * 生成指定试卷doc
     *
     * @param paperVOList
     */
    private String produceDoc(List<EssayExportPaperVO> paperVOList, int type) {
        boolean flag = false;
        String randomStr = UUID.randomUUID().toString().replaceAll("-", "");
        String path = filePath + randomStr;
        File directoryFile = new File(path);
        if (!directoryFile.exists()) {
            directoryFile.mkdir();
        }
        for (EssayExportPaperVO paperVO : paperVOList) {
            String newPath = path + "/" + paperVO.getPaperName() + ".doc";
            log.info("docPath：" + newPath);
            // 设置纸张大小
            Document document = null;
            RtfWriter2 writer = null;
            String paperName = paperVO.getPaperName();
            try {
                // 设置纸张大小
                document = new Document(PageSize.A4);


                File file = new File(newPath);

                writer = RtfWriter2.getInstance(document, new FileOutputStream(file));

                // 设置中文字体
                BaseFont bfChinese = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
                // 标题字体风格
                Font titleFont = new Font(bfChinese, 16, Font.BOLD);
                //二级标题标题字体风格
                Font subTitleFont = new Font(bfChinese, 14, Font.BOLD);

                // 正文字体风格
                Font contentFont = new Font(bfChinese, 10, Font.NORMAL);
                document.open();

                //页眉
//            HeaderFooter header = new HeaderFooter(new Phrase("过关才是硬道理"), false);
//            header.setAlignment(Rectangle.ALIGN_CENTER);
//            document.setHeader(header);
                //录入标题
                addContent(paperName, document, titleFont, Element.ALIGN_CENTER);

                //录入材料
                addContent("【给定资料】", document, subTitleFont, Element.ALIGN_LEFT);

                List<EssayMaterial> materialList = paperVO.getMaterialList();
                for (EssayMaterial material : materialList) {
                    addContent(material.getContent(), document, contentFont, Element.ALIGN_LEFT);
                }
                //遍历试题
                List<EssayExportQuestionVO> questionList = paperVO.getQuestionList();
                for (EssayExportQuestionVO questionVO : questionList) {

                    String stem = questionVO.getStem();
                    List<EssayStandardAnswer> answerList = questionVO.getAnswerList();
                    String analysis = questionVO.getAnalysis();
                    String review = questionVO.getReview();
                    addContent("【作答要求】", document, subTitleFont, Element.ALIGN_LEFT);
                    addContent(stem, document, contentFont, Element.ALIGN_LEFT);

                    if (type == EssayExportReqVO.BASE_CONTENT_WITH_ANSWER || type == EssayExportReqVO.BASE_CONTENT_WITH_ANSWER_AND_ANALYSIS) {
                        if (answerList.get(0).getAnswerFlag() == 0) {
                            addContent("【参考答案】", document, subTitleFont, Element.ALIGN_LEFT);
                        } else {
                            addContent("【标准答案】", document, subTitleFont, Element.ALIGN_LEFT);
                        }
                        addContent(answerList.get(0).getTopic(), document, contentFont, Element.ALIGN_CENTER);
                        addContent(answerList.get(0).getSubTopic(), document, contentFont, Element.ALIGN_CENTER);
                        addContent(answerList.get(0).getCallName(), document, contentFont, Element.ALIGN_LEFT);
                        addContent(answerList.get(0).getAnswerComment(), document, contentFont, Element.ALIGN_LEFT);
                        String inscribedName = answerList.get(0).getInscribedName();
                        if (StringUtils.isNotEmpty(inscribedName)) {
                            String[] split = inscribedName.split("<br/>");
                            for (int i = 0; i < split.length; i++) {
                                addContent(split[i], document, contentFont, Element.ALIGN_RIGHT);
                            }
                        }
                        addContent(answerList.get(0).getInscribedDate(), document, contentFont, Element.ALIGN_RIGHT);
                    }

                    if (type == EssayExportReqVO.BASE_CONTENT_WITH_ANSWER_AND_ANALYSIS) {
                        addContent("【解析】", document, subTitleFont, Element.ALIGN_LEFT);
                        addContent(getDifficultGradeStr(questionVO.getDifficultGrade()), document, contentFont, Element.ALIGN_LEFT);
                        addContent(analysis, document, contentFont, Element.ALIGN_LEFT);

                        addContent("【经验小结】", document, subTitleFont, Element.ALIGN_LEFT);
                        addContent(review, document, contentFont, Element.ALIGN_LEFT);
                    }
                }

                //关闭文档
                document.close();
                //关闭书写器
                writer.close();
            } catch (Exception e) {
                //关闭文档
                document.close();
                //关闭书写器
                if (writer != null) {
                    writer.close();
                }
                e.printStackTrace();
            }
        }

        try {
            String str = "申论试题" + DateFormatUtils.format(new Date(), "yyyyMMdd") + ExportTypeEnum.create(type).getTitle();
            String pat = filePath + str + randomStr + ".zip";
            ZipUtils.doCompress(path, pat);
            String finalPath = upload(pat);
            return finalPath;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String contentFilter(String content, int type) {
        if (StringUtils.isNotEmpty(content)) {
            content = content.replaceAll("<br/>", "\n").replaceAll("</p>", "\n").replaceAll("<[^>]+>", "");
            if (type == EssayExportReqVO.FILE_TYPE_WORD) {
                content = content.replaceAll("(&nbsp;){6,8}", PdfUtil.WORD_HEAD_BLANK);
            } else if (type == EssayExportReqVO.FILE_TYPE_PDF) {
                content = content.replaceAll("(&nbsp;){6,8}", PdfUtil.PDF_HEAD_BLANK);
            }

            content = content.replace("&lt;", "<");
            content = content.replace("&gt;", ">");
            if (content.endsWith("\n")) {
                content = content.substring(0, content.lastIndexOf("\n"));
            }
        } else {
            content = "";
        }
        return content;
    }

    public void addContent(String content, Document document, Font font, int alignment) throws DocumentException {
        if (StringUtils.isNotEmpty(content)) {
//            content = contentFilter(content, EssayExportReqVO.FILE_TYPE_WORD);
            //标题
            Paragraph contentParagraph = new Paragraph();
            List<TextStyleElement> elements = CommonFileUtil.assertTextStyle(content);
            List<Map<String, Integer>> regions = CommonFileUtil.getContentRegions(elements, content);
            for (Map<String, Integer> region : regions) {
                List<TextStyleEnum> styles = CommonFileUtil.getStyles(elements, region);
                int fontStyle = CommonFileUtil.countFontStyle(styles, font);
                Font tempFont = new Font(font.getBaseFont(), font.getSize(), fontStyle);
                String substring = content.substring(region.get("start"), region.get("end") + 1);
                Phrase phrase = new Phrase(contentFilter(substring, EssayExportReqVO.FILE_TYPE_WORD), tempFont);
                contentParagraph.add(phrase);
            }
            // 设置段落间隔
            contentParagraph.setSpacingBefore(5);
            //设置对齐方式
            contentParagraph.setAlignment(alignment);
            contentParagraph.setFont(font);
            document.add(contentParagraph);
        }
    }

    /**
     * 关键词关键句写入
     *
     * @param front
     * @param end
     * @param document
     * @param littleFont
     * @param font
     * @param alignment
     * @throws DocumentException
     */
    public void addKeyWordContent(String front, String end, Document document, Font littleFont, Font font, int alignment) throws DocumentException {
        if (StringUtils.isNotEmpty(front)) {
            Paragraph contentParagraph = new Paragraph();

            Chunk chunk = new Chunk(front, littleFont);
            Chunk contentChunk = new Chunk(end, font);
            contentParagraph.add(chunk);
            contentParagraph.add(contentChunk);
            // 设置段落间隔
            contentParagraph.setSpacingBefore(5);
            //设置对齐方式
            contentParagraph.setAlignment(alignment);
            contentParagraph.setFont(font);
            document.add(contentParagraph);
        }

    }


    private List<EssayExportPaperVO> findPaperVOList(EssayExportReqVO vo) {
        List<Long> paperIdList = vo.getPaperIdList();
        List<EssayExportPaperVO> essayExportPaperVOList = new LinkedList<>();

        for (Long paperId : paperIdList) {
            EssayPaperBase essayPaperBase = essayPaperBaseRepository.findOne(paperId);
            //试卷材料列表
            List<EssayMaterial> materialList = essayMaterialRepository.findByPaperIdAndStatusOrderBySortAsc
                    (paperId, EssayMaterialConstant.EssayMaterialStatusEnum.NORMAL.getStatus());

            //组装试题列表
            List<EssayExportQuestionVO> questionVOList = new LinkedList<>();
            List<EssayQuestionBase> essayQuestionBaseList = essayQuestionBaseRepository.findByPaperIdAndStatusOrderBySortAsc
                    (paperId, EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
            List<Long> detailIds = essayQuestionBaseList.stream().map(EssayQuestionBase::getDetailId).collect(Collectors.toList());
            List<EssayQuestionDetail> questionDetails = essayQuestionDetailRepository.findByIdIn(detailIds);
            //组装试题
            for (EssayQuestionBase base : essayQuestionBaseList) {
                List<EssayStandardAnswer> essayStandardAnswerList = essayStandardAnswerRepository.findByQuestionIdAndStatusOrderByIdAsc
                        (base.getDetailId(), EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
                List<EssayQuestionDetail> details = questionDetails.stream().filter(i -> base.getDetailId() == i.getId()).collect(Collectors.toList());
                //查询算法
                if (CollectionUtils.isNotEmpty(details)) {
                    EssayQuestionDetail detail = details.get(0);
                    //关键词语
                    AdminQuestionKeyRuleVO keyPhraseAndKeyRuleVO = essayRuleController.findKeyPhraseAndKeyWord(detail.getId(), null, "-1");
                    //格式规则
                    AdminQuestionFormatVO questionFormatVO = essayRuleController.findAnswerFormatByQuestion(detail.getId());
                    log.info("detailId是:{},结果是:{},格式内容:{}", detail.getId(),
                            JsonUtil.toJson(keyPhraseAndKeyRuleVO), JsonUtil.toJson(questionFormatVO));
                    //扣分标准
                    //AdminQuestionDeductRuleVO deductRule = essayRuleController.findDeductRule(detail.getId(), null, null);
                    EssayQuestionDeductRuleVO resultVO = EssayQuestionDeductRuleVO.builder()
                            .commonRuleList(essayRuleController.findCommonAnswerRule(detail.getId()))
                            .specialStripList(essayRuleController.findAnswerRuleSpecialStripByQuestion(detail.getId()))
                            .questionDetailId(detail.getId()).build();

                    EssayExportQuestionVO questionVO = EssayExportQuestionVO.builder()
                            .stem(detail.getAnswerRequire())
                            .answerList(essayStandardAnswerList)
                            .analysis(detail.getAnalyzeQuestion())
                            .review(detail.getAuthorityReviews())
                            .difficultGrade(detail.getDifficultGrade())
                            .sort(base.getSort())
                            .correctType(detail.getCorrectType())
                            .questionKeyRuleVO(keyPhraseAndKeyRuleVO)
                            .questionType(detail.getType())
                            .questionFormatVO(questionFormatVO)
                            .deductRuleVO(resultVO)
                            .build();
                    log.info("试题信息是:{}", JsonUtil.toJson(questionVO.getQuestionKeyRuleVO()));
                    questionVOList.add(questionVO);
                }
            }

            EssayExportPaperVO paperVO = EssayExportPaperVO.builder()
                    .materialList(materialList)
                    .paperId(paperId)
                    .paperName(essayPaperBase.getName())
                    .questionList(questionVOList)
                    .build();

            essayExportPaperVOList.add(paperVO);
        }

        return essayExportPaperVOList;

    }

    /**
     * 整合关键词
     *
     * @param questionDetailId
     * @param type
     * @return
     */
    public AdminExportKeyWordAndPhraseVO combinePhraseAndKeyWord(Long questionDetailId, int type) {
        //带描述的关键字
        AdminExportKeyWordAndPhraseVO adminExportKeyWordAndPhraseVO = new AdminExportKeyWordAndPhraseVO();
        if (type == EssayAnswerKeyWordConstant.QUESTION_PARENT_KEYWORD_WITH_DESC) {
            List<AdminQuestionKeyWordWithDescVO> keyWordWithDescList = essayRuleService.findAnswerKeyWordByQuestionWithDesc(questionDetailId);
            if (CollectionUtils.isNotEmpty(keyWordWithDescList)) {
                adminExportKeyWordAndPhraseVO.setAdminQuestionKeyWordWithDescVO(keyWordWithDescList);
            }
            //不带描述的关键字
        } else if (type == QUESTION_PARENT_KEYWORD_WITHOUT_DESC) {
            List<AdminQuestionKeyWordVO> answerKeyWordByQuestion = essayRuleController.findAnswerKeyWordByQuestion(questionDetailId);
            if (CollectionUtils.isNotEmpty(answerKeyWordByQuestion)) {
                adminExportKeyWordAndPhraseVO.setAdminQuestionKeyWordVO(answerKeyWordByQuestion);
            }
        }
        return adminExportKeyWordAndPhraseVO;
    }

    enum ExportTypeEnum {

        BASE_CONTENT(0, "(材料+问题)"),
        BASE_CONTENT_WITH_ANSWER(1, "(材料+问题+答案)"),
        BASE_CONTENT_WITH_ANSWER_AND_ANALYSIS(2, "(材料+问题+答案+试题分析+经验小结)");


        private Integer value;
        private String title;

        ExportTypeEnum(Integer value, String title) {
            this.value = value;
            this.title = title;
        }

        public static ExportTypeEnum create(Integer value) {
            for (ExportTypeEnum exportTypeEnum : values()) {
                if (value.equals(exportTypeEnum.getValue())) {
                    return exportTypeEnum;
                }
            }
            return null;
        }

        public Integer getValue() {
            return value;
        }

        public String getTitle() {
            return title;
        }

    }

    private String getDifficultGradeStr(double difficultGrade) {
        String difficultGradeStr = "";
        switch ((int) difficultGrade) {
            case 0:
                break;
            case 1:
                difficultGradeStr = "    难度：较小";
                break;
            case 2:
                difficultGradeStr = "    难度：适中";
                break;
            case 3:
                difficultGradeStr = "    难度：较大";
                break;
        }
        return difficultGradeStr;
    }


    private String getAnswerDesc(EssayQuestionAnswer questionAnswer) {
        String desc = "";
        if (null != questionAnswer) {
            desc = "答题卡ID：" + questionAnswer.getId()
                    + "\t\t本题得分 " + questionAnswer.getExamScore() + "/" + questionAnswer.getScore() + "分"
                    + "\t\t字数：" + questionAnswer.getInputWordNum() + "字"
                    + "\t\t用时：" + questionAnswer.getSpendTime() / 60 + "'" + questionAnswer.getSpendTime() % 60 + "''";
        }

        return desc;
    }


}
