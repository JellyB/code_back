package com.huatu.tiku.teacher.service.impl.download.v1;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.common.utils.date.DateFormatUtil;
import com.huatu.tiku.entity.download.BaseTool;
import com.huatu.tiku.entity.knowledge.Knowledge;
import com.huatu.tiku.entity.teacher.PaperModuleInfo;
import com.huatu.tiku.enums.EnumUtil;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.enums.QuestionElementEnum;
import com.huatu.tiku.enums.QuestionInfoEnum;
import com.huatu.tiku.teacher.service.download.v1.DownloadWriteServiceV1;
import com.huatu.tiku.teacher.service.download.v1.WordWriteServiceV1;
import com.huatu.tiku.teacher.service.download.v1.ZipUtil;
import com.huatu.tiku.teacher.service.knowledge.KnowledgeService;
import com.huatu.tiku.teacher.util.file.CommonFileUtil;
import com.huatu.tiku.teacher.util.file.TextStyleElement;
import com.huatu.tiku.util.file.FunFileUtils;
import com.huatu.tiku.util.file.UploadFileUtil;
import com.huatu.tiku.util.html.HtmlConvertUtil;
import com.lowagie.text.*;
import com.lowagie.text.rtf.RtfWriter2;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.io.*;
import java.time.format.TextStyle;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by huangqingpeng on 2018/8/15.
 */
@Slf4j
@Service
public class WordWriteServiceImplV1 implements WordWriteServiceV1 {

    public final static String[] num_lower = {"一", "二", "三", "四", "五", "六", "七", "八", "九"};

    /**
     * word 文件名后缀
     */
    public final static String WORD_TAIL_NAME = ".doc";

    @Autowired
    DownloadWriteServiceV1 downloadWriteServiceV1;

    @Autowired
    KnowledgeService knowledgeService;


    @Override
    public String download(long paperId, PaperInfoEnum.TypeInfo typeInfo, QuestionElementEnum.QuestionFieldEnum exportType) {
        StopWatch stopWatch = new StopWatch("download:" + paperId);
        stopWatch.start("makeWordByPaper");
        String fileName = makeWordByPaper(paperId, typeInfo, exportType, "", false);
        stopWatch.stop();
        stopWatch.start("ftp");
        if (StringUtils.isBlank(fileName)) {
            log.error("下载失败，paperid={}，typeInfo={},exportType={}", paperId, typeInfo.getName(), exportType.getValue());
            throw new BizException(ErrorResult.create(1001010, "下载失败"));
        }
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
        stopWatch.stop();
        log.info("downWord:{}", stopWatch.prettyPrint());
        //cdn文件下载路径
        return FunFileUtils.WORD_FILE_SAVE_URL + name + tailName;
    }

    private String makeWordByPaper(long paperId, PaperInfoEnum.TypeInfo typeInfo, QuestionElementEnum.QuestionFieldEnum exportType, String moduleName, boolean duplicateFlag) {
        WordWriteTool wordWriteTool = new WordWriteTool();
        wordWriteTool.setTypeInfo(typeInfo);
        wordWriteTool.setExportType(exportType);
        wordWriteTool.setModuleName(moduleName);
        wordWriteTool.setDuplicateFlag(duplicateFlag);
        try {
            String result = downloadWriteServiceV1.makeWordByPaper(paperId, wordWriteTool, createWriteTool(), writeTitle(), writeModule(), writeQuestion());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            wordWriteTool.close();
        }
        return "";
    }

    public BiConsumer<BaseTool, Map<String, Object>> writeQuestion() {
        return ((baseTool, questionMap) -> {
            if (baseTool instanceof WordWriteTool) {
                try {
                    addQuestionElement((WordWriteTool) baseTool,
                            questionMap,
                            ((WordWriteTool) baseTool).getExportType());
                } catch (Exception e) {
                    log.error(e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    public BiConsumer<BaseTool, PaperModuleInfo> writeModule() {
        return ((baseTool, paperModuleInfo) -> {
            if (baseTool instanceof WordWriteTool) {
                try {
                    addModuleElement(((WordWriteTool) baseTool).getDocument(), ((WordWriteTool) baseTool).getModuleFont(), paperModuleInfo.getName(), paperModuleInfo.getId());
                } catch (DocumentException e) {
                    log.error(e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 写入标题
     *
     * @return
     */
    public BiConsumer<BaseTool, String> writeTitle() {
        return (BaseTool baseTool, String s) -> {
            if (baseTool instanceof WordWriteTool) {
                WordWriteTool wordWriteTool = (WordWriteTool) baseTool;
                Document document = wordWriteTool.getDocument();
                Font font = wordWriteTool.getTitleFont();
                try {
                    addTitleElement(document, s, font);
                } catch (DocumentException e) {
                    log.error(e.getMessage());
                    e.printStackTrace();
                }
            }
        };
    }

    /**
     * 创建或者补充写入word工具类参数
     *
     * @return
     */
    public BiFunction<BaseTool, String, BaseTool> createWriteTool() {
        BiFunction<BaseTool, String, BaseTool> function = ((tool, name) -> {
            WordWriteTool wordWriteTool = null;
            String namePath = name.replace("/", "");
            if (null == tool) {
                wordWriteTool = new WordWriteTool();
            }
            if (tool instanceof WordWriteTool) {
                wordWriteTool = (WordWriteTool) tool;
                File file = new File(wordWriteTool.getDir() + namePath + wordWriteTool.getSuffix());
                Document document = null;
                RtfWriter2 rtfWriter2 = null;
                try {
                    document = CommonFileUtil.newDocument();
                    //正文试题字体
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    rtfWriter2 = RtfWriter2.getInstance(document, fileOutputStream);
                    document.open();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                wordWriteTool.setFile(file);
                wordWriteTool.setDocument(document);
                wordWriteTool.setRtfWriter2(rtfWriter2);
                return wordWriteTool;
            }
            return wordWriteTool;
        });
        return function;
    }

    @Override
    public String downLoadList(List<Long> paperIds, PaperInfoEnum.TypeInfo typeInfo, QuestionElementEnum.QuestionFieldEnum exportType, String moduleName, boolean duplicateFlag) throws com.huatu.ztk.commons.exception.BizException {
        List<String> fileNames = Lists.newArrayList();
        LinkedList<String> linkedList = Lists.newLinkedList();
        paperIds.parallelStream().forEach(paperId -> {
            String fileName = makeWordByPaper(paperId, typeInfo, exportType, moduleName, duplicateFlag);
            if (StringUtils.isNotBlank(fileName)) {
                linkedList.add(fileName.replace(WORD_TAIL_NAME, ""));
            }
        });
        if (CollectionUtils.isEmpty(linkedList)) {
            return "";
        }
        fileNames.addAll(linkedList);
        return ZipUtil.zipFile(fileNames, WORD_TAIL_NAME, FunFileUtils.TMP_WORD_SOURCE_FILEPATH);
    }


    @Override
    public void addQuestionElement(Document document, Font font, Map<String, Object> questionMap, QuestionElementEnum.QuestionFieldEnum exportType) throws Exception {

    }

    /**
     * 下载word -- 试题内容添加
     *
     * @param baseTool
     * @param questionMap
     * @param exportType
     * @throws Exception
     */
    public void addQuestionElement(WordWriteTool baseTool, Map<String, Object> questionMap, QuestionElementEnum.QuestionFieldEnum exportType) throws Exception {
        long start = System.currentTimeMillis();
        StopWatch stopWatch = new StopWatch("addQuestionElement：" + questionMap.getOrDefault("questionId", -1));
        int type = Integer.parseInt(questionMap.getOrDefault("type", "-1").toString());
        if (type < 0) {
            log.error("题型属性不具备：map={}", questionMap);
            return;
        }
        QuestionInfoEnum.QuestionSaveTypeEnum questionSaveTypeEnum = QuestionInfoEnum.getSaveTypeByQuestionType(type);
        QuestionElementEnum.QuestionOperateEnum questionOperateEnum = QuestionElementEnum.QuestionOperateEnum.create(questionSaveTypeEnum);
        //元素操作
        List<QuestionElementEnum.ElementEnum> values = questionOperateEnum.getValue();
        boolean sortFlag = false;       //sortFlag是否需要携带
        for (QuestionElementEnum.ElementEnum element : values) {
            if (element.equals(QuestionElementEnum.ElementEnum.SORT) && exportType.equals(QuestionElementEnum.QuestionFieldEnum.ANSWER)) {
                sortFlag = true;
            }
            if (element.getLocation().equals(QuestionElementEnum.LocationEnum.UN_KNOW)) {
                continue;
            }
            //只处理符合类型的标签
            if (exportType.equals(QuestionElementEnum.QuestionFieldEnum.COMMON) ||      //下载方式下载所有数据
                    element.getField().equals(QuestionElementEnum.QuestionFieldEnum.COMMON) ||      //数据属于共有数据
                    element.getField().equals(exportType)) {        //数据类型和下载类型一致
                String value = String.valueOf(questionMap.get(element.getKey()));
                if ("null".equals(value) || StringUtils.isBlank(value)) {
                    continue;
                }
                String answerSort = "";
                if (sortFlag) {       //在题序元素之后，一个非空元素的格式之前加入题序元素
                    answerSort = String.valueOf(questionMap.getOrDefault(QuestionElementEnum.ElementEnum.SORT.getKey(), ""));
                    answerSort = answerSort + "、";      //附加顿号格式
                    sortFlag = false;
                }
                if (element.equals(QuestionElementEnum.ElementEnum.TYPE)) {
                    value = EnumUtil.valueOf(Integer.parseInt(value), QuestionInfoEnum.QuestionTypeEnum.class); //题型数据变成名称
                }
                if (element.getLocation().equals(QuestionElementEnum.LocationEnum.OLD_LINE)) {
                    stopWatch.start("add " + element.getName());
                    CommonFileUtil.fillContent(answerSort + element.getName() + value, baseTool, addText());
                    stopWatch.stop();
                    continue;
                } else if (element.getLocation().equals(QuestionElementEnum.LocationEnum.NEW_LINE)) {
                    stopWatch.start("add " + element.getName());
                    CommonFileUtil.fillContent(answerSort + element.getName(), baseTool, addText());
                    CommonFileUtil.fillContent(value, baseTool, addText());
                    stopWatch.stop();
                    continue;
                }
                //其他自定义的展示情况
                if (element.equals(QuestionElementEnum.ElementEnum.STEM)) {
                    stopWatch.start("add " + element.getName());
                    String sort = String.valueOf(questionMap.getOrDefault(QuestionElementEnum.ElementEnum.SORT.getKey(), "-1"));
                    if (!"-1".equals(sort)) {
                        CommonFileUtil.fileExistsImg(sort + element.getName(), value, baseTool, addText());
                    } else {
                        CommonFileUtil.fillContent(value, baseTool, addText());
                    }
                    stopWatch.stop();
                    continue;
                }
                if (element.equals(QuestionElementEnum.ElementEnum.CHOICE)) {
                    stopWatch.start("add " + element.getName());
                    List<String> choices = HtmlConvertUtil.parseChoices(value);
                    for (int i = 0; i < choices.size(); i++) {
                        char perChar = (char) ('A' + i);
                        CommonFileUtil.fileExistsImg(perChar + element.getName(), choices.get(i), baseTool, addText());
                    }
                    stopWatch.stop();
                    continue;
                }
                if (element.equals(QuestionElementEnum.ElementEnum.MATERIAL)) {
                    stopWatch.start("add " + element.getName());
                    String[] split = value.split(element.getName());
                    CommonFileUtil.fillContent(element.getName(), baseTool, addText());
                    for (String s : split) {
                        CommonFileUtil.fileExistsImg("", s, baseTool, addText());
                    }
                    stopWatch.stop();
                }
            }
        }
        long time = System.currentTimeMillis() - start;
        if (time > TimeUnit.SECONDS.toMillis(1)) {
            log.info("time>3 stopWatch:{}", stopWatch.prettyPrint());
        }
        // 换行
        baseTool.getDocument().add(new Paragraph(" "));
    }

    @Override
    public void addModuleElement(Document document, String moduleName, int sort) throws DocumentException {
        Font font = CommonFileUtil.newFont(16, Font.BOLD);
        addModuleElement(document, font, moduleName, sort);
    }

    private void addModuleElement(Document document, Font font, String moduleName, int sort) throws DocumentException {
        Paragraph module = new Paragraph(moduleName);
        // 设置标题格式对齐方式
        module.setAlignment(Element.ALIGN_CENTER);
        module.setFont(font);
        document.add(module);
        // 换行
        document.add(new Paragraph(" "));
    }


    @Override
    public void addTitleElement(Document document, String name) throws DocumentException {
        Font font = CommonFileUtil.newFont(20, Font.BOLD);
        addTitleElement(document, name, font);
    }

    @Override
    public String downloadGroupByModule(List<Long> ids, PaperInfoEnum.TypeInfo typeInfo, QuestionElementEnum.QuestionFieldEnum questionFieldEnum) throws com.huatu.ztk.commons.exception.BizException {
        List<String> moduleNames = downloadWriteServiceV1.getModuleNames(ids, typeInfo);
        ArrayList<String> list = Lists.newArrayList();
        for (String moduleName : moduleNames) {
            String path = makeWordByModule(ids, typeInfo, questionFieldEnum, moduleName, true);
            if (StringUtils.isNotBlank(path)) {
                list.add(path.replace(WORD_TAIL_NAME, ""));
            }
        }
        if (CollectionUtils.isEmpty(list)) {
            return "";
        }
        return ZipUtil.zipFile(list, WORD_TAIL_NAME, FunFileUtils.TMP_WORD_SOURCE_FILEPATH);
    }

    @Override
    public String downloadTopPoint(Long pointId, int size, QuestionElementEnum.QuestionFieldEnum questionFieldEnum) {
        List<Knowledge> all = knowledgeService.findAll();

        return null;
    }

    @Override
    public String downloadByQuestionWithKnowledge(Long pointId, String ids, QuestionElementEnum.QuestionFieldEnum questionFieldEnum) throws com.huatu.ztk.commons.exception.BizException {
        Knowledge knowledge = knowledgeService.selectByPrimaryKey(pointId);
        String path = makeWordByQuestion(ids, questionFieldEnum, knowledge.getName());
        ArrayList<String> list = Lists.newArrayList(path.replace(WORD_TAIL_NAME, ""));
        return ZipUtil.zipFile(list, WORD_TAIL_NAME, FunFileUtils.TMP_WORD_SOURCE_FILEPATH);

    }

    private String makeWordByQuestion(String ids, QuestionElementEnum.QuestionFieldEnum questionFieldEnum, String name) {
        WordWriteTool wordWriteTool = new WordWriteTool();
        wordWriteTool.setExportType(questionFieldEnum);
        wordWriteTool.setModuleName(name);
        wordWriteTool.setDuplicateFlag(false);
        List<Integer> questionIds = Arrays.stream(ids.split(",")).map(Integer::parseInt).collect(Collectors.toList());
        try {
            if (CollectionUtils.isNotEmpty(questionIds)) {
                BaseTool tool = createWriteTool().apply(wordWriteTool, name);
                writeTitle().accept(tool, name);
                downloadWriteServiceV1.writeQuestions(tool, writeQuestion(), questionIds, questionFieldEnum);
                return tool.getFile().getName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            wordWriteTool.close();
        }
        return "";
    }

    private String makeWordByModule(List<Long> ids, PaperInfoEnum.TypeInfo typeInfo, QuestionElementEnum.QuestionFieldEnum exportType, String moduleName, boolean duplicateFlag) {
        WordWriteTool wordWriteTool = new WordWriteTool();
        wordWriteTool.setTypeInfo(typeInfo);
        wordWriteTool.setExportType(exportType);
        wordWriteTool.setModuleName(moduleName);
        wordWriteTool.setDuplicateFlag(duplicateFlag);
        List<Integer> questionIds = downloadWriteServiceV1.getQuestionByModule(ids, typeInfo, duplicateFlag, moduleName);
        try {
            if (CollectionUtils.isNotEmpty(questionIds)) {
                BaseTool tool = createWriteTool().apply(wordWriteTool, moduleName);
                writeTitle().accept(tool, moduleName);
                downloadWriteServiceV1.writeQuestions(tool, writeQuestion(), questionIds, exportType);
                return tool.getFile().getName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            wordWriteTool.close();
        }
        return "";
    }

    private void addTitleElement(Document document, String name, Font font) throws DocumentException {
        Paragraph title = new Paragraph(name);
        // 设置标题格式对齐方式
        title.setAlignment(Element.ALIGN_CENTER);
        title.setFont(font);
        document.add(title);
        // 换行
        document.add(new Paragraph(" "));
        // 换行
        document.add(new Paragraph(" "));
    }

    BiConsumer<BaseTool, Object> addText() {
        return (baseTool, o) -> {
            if (baseTool instanceof WordWriteTool) {
                Document document = ((WordWriteTool) baseTool).getDocument();
                if (!document.isOpen()) {
                    document.open();
                }
                Font questionFont = ((WordWriteTool) baseTool).getQuestionFont();
                try {
                    if (o instanceof String) {
                        Paragraph paragraph1 = new Paragraph("", questionFont);
                        String s = String.valueOf(o);
                        //写入文本内容
                        handlerTextFont(paragraph1, s, questionFont);
//                        handlerBlodFont(paragraph1, s, questionFont);
                        paragraph1.setIndentationLeft(20);
                        document.add(paragraph1);
                    } else if (o instanceof HashMap) {
                        Image imageInfo = getImageInfo((HashMap) o);
                        if (null != imageInfo) {
                            document.add(imageInfo);
                        }
                    } else if (o instanceof List) {
                        Paragraph paragraph = new Paragraph("", questionFont);
                        paragraph.setAlignment(Element.ALIGN_MIDDLE);
                        for (Object part : (List) o) {
                            if (part instanceof String) {
                                String s = String.valueOf(part);
//                                handlerBlodFont(paragraph, s, questionFont);
                                handlerTextFont(paragraph, s, questionFont);
                            } else if (part instanceof HashMap) {
                                Image imageInfo = getImageInfo((HashMap) part);
                                if (null != imageInfo) {
                                    paragraph.add(imageInfo);
                                }
                            }
                        }
                        paragraph.setIndentationLeft(20);
                        document.add(paragraph);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    /**
     * 文本内容写入
     *
     * @param paragraph
     * @param content
     * @param questionFont
     */
    private void handlerTextFont(Paragraph paragraph, String content, Font questionFont) {
        List<TextStyleElement> list = CommonFileUtil.assertTextStyle(content);
        List<Map<String, Integer>> regions = CommonFileUtil.getContentRegions(list, content);
        for (Map<String, Integer> region : regions) {
            List<QuestionElementEnum.TextStyleEnum> styles = CommonFileUtil.getStyles(list, region);
            int fontStyle = CommonFileUtil.countFontStyle(styles, questionFont);
            Font font = new Font(questionFont.getBaseFont(), questionFont.getSize(), fontStyle);
            Phrase phrase = new Phrase(content.substring(region.get("start"), region.get("end") + 1), font);
            paragraph.add(phrase);
        }
    }

//    /**
//     * 子题内容写入文本
//     *
//     * @param paragraph
//     * @param content
//     * @param questionFont
//     */
//    private void handlerBlodFont(Paragraph paragraph, String content, Font questionFont) {
//        Pattern pattern = Pattern.compile("<strong>([^<]+)</strong>");
//        Matcher matcher = pattern.matcher(content);
//        int i = 0;
//        while (matcher.find(i)) {
//            int start = matcher.start();
//            int end = matcher.end();
//            if (i < start) {
//                paragraph.add(content.substring(i, start));
//            }
//            String group = matcher.group(1);
//            addBoldFont2Paragraph(paragraph, group, questionFont);
//            i = end;
//        }
//        if (content.length() > i) {
//            paragraph.add(content.substring(i, content.length()));
//        }
//    }
//
//    /**
//     * 往段落中添加加粗字体的内容
//     *
//     * @param paragraph
//     * @param group
//     * @param questionFont
//     */
//    private void addBoldFont2Paragraph(Paragraph paragraph, String group, Font questionFont) {
//        Font font = new Font(questionFont.getBaseFont(), questionFont.getSize(), Font.BOLD);
//        Phrase phrase = new Phrase(group, font);
//        paragraph.add(phrase);
//    }

    private Image getImageInfo(HashMap mapData) throws BadElementException, com.huatu.ztk.commons.exception.BizException, IOException {
        //start~end 是图片信息组成Image信息
        String imgTag = MapUtils.getString(mapData, "imgTag");
        Object img = CommonFileUtil.assertImg(imgTag);
        if (img instanceof Image) {
            return (Image) img;
        }
        return null;
    }

    private void addStringText(Document document, Font questionFont, Paragraph paragraph, String content) throws DocumentException {
        if (null == paragraph) {
            paragraph = new Paragraph(String.valueOf(content), questionFont);
            paragraph.setIndentationLeft(20);
            document.add(paragraph);
        } else {
            paragraph.add(content);
        }
    }

    @Data
    public class WordWriteTool extends BaseTool {
        private Document document;
        private RtfWriter2 rtfWriter2;
        private final Font titleFont = CommonFileUtil.newFont(21, Font.NORMAL);
        private final Font moduleFont = CommonFileUtil.newFont(18, Font.NORMAL);
        private final Font questionFont = CommonFileUtil.newFont(11, Font.NORMAL);

        public WordWriteTool() {
            super(FunFileUtils.TMP_WORD_SOURCE_FILEPATH, WORD_TAIL_NAME, null, 0, 1,-1, 0, "", false, null, null, true);
        }

        public void close() {
            if (null != document) {
                document.close();
            }
            if (null != rtfWriter2) {
                rtfWriter2.close();
            }
            if (null != getFile() && getFile().exists()) {
                getFile().deleteOnExit();
            }

        }
    }
}
