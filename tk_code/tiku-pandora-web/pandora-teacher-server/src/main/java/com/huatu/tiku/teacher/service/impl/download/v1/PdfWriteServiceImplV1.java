package com.huatu.tiku.teacher.service.impl.download.v1;

import com.google.common.collect.Lists;
import com.google.zxing.WriterException;
import com.huatu.tiku.constants.cache.RedisKeyConstant;
import com.huatu.tiku.entity.download.BaseTool;
import com.huatu.tiku.entity.teacher.PaperModuleInfo;
import com.huatu.tiku.enums.EnumUtil;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.enums.QuestionElementEnum;
import com.huatu.tiku.enums.QuestionInfoEnum;
import com.huatu.tiku.teacher.service.download.v1.DownloadWriteServiceV1;
import com.huatu.tiku.teacher.service.download.v1.PdfWriteServiceV1;
import com.huatu.tiku.teacher.util.file.*;
import com.huatu.tiku.teacher.util.image.NewImageUtils;
import com.huatu.tiku.util.file.CourseQCode;
import com.huatu.tiku.util.file.FunFileUtils;
import com.huatu.tiku.util.file.UploadFileUtil;
import com.huatu.tiku.util.html.HtmlConvertUtil;
import com.huatu.ztk.paper.bean.PracticeCard;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.Cleanup;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Created by huangqingpeng on 2018/11/6.
 */
@Slf4j
@Service
public class PdfWriteServiceImplV1 implements PdfWriteServiceV1 {
    private final static String HEAD_QCODE_IMAGE_URL = UploadFileUtil.IMG_URL + "question-qcode-back-ground.png";
    private final static String TAIL_QCODE_IMAGE_URL = UploadFileUtil.IMG_URL + "answer-qcode-back-ground-1.png";

    public final static String PDF_TAIL_NAME = ".pdf";
    @Autowired
    DownloadWriteServiceV1 downloadWriteServiceV1;
    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public String download(long paperId, PaperInfoEnum.TypeInfo typeInfo, QuestionElementEnum.QuestionFieldEnum exportType, Map mapData) throws IOException, DocumentException {
        String fileName = makeWordByPaper(paperId, typeInfo, exportType);       //得到本地文件
        if (StringUtils.isBlank(fileName)) {
            return "下载失败";
        }
        //cdn上传下载文件后缀
        File file = new File(FunFileUtils.TMP_PDF_SOURCE_FILEPATH + fileName);
        if (file.exists() && file.isFile()) {
            BigDecimal divide = new BigDecimal(file.length()).divide(new BigDecimal(1024));
            mapData.put("size", divide.setScale(0, BigDecimal.ROUND_HALF_UP).toString() + "KB");
        }
        String tempName = UUID.randomUUID() + PDF_TAIL_NAME;
        try {
            //cdn上传需要的文件名
            UploadFileUtil.getInstance().ftpUploadFile(file, tempName, FunFileUtils.PDF_FILE_SAVE_PATH);
            FunFileUtils.deleteFile(file);
        } finally {
            if (file.exists()) {
                FunFileUtils.deleteFile(file);
            }
        }
        //cdn文件下载路径
        return FunFileUtils.PDF_FILE_SAVE_URL + tempName;
    }

    private String makeWordByPaper(long paperId, PaperInfoEnum.TypeInfo typeInfo, QuestionElementEnum.QuestionFieldEnum exportType) throws IOException, DocumentException {
        PdfWriteTool pdfWriteTool = null;
        try {
            pdfWriteTool = new PdfWriteTool();
            pdfWriteTool.setTypeInfo(typeInfo);
            pdfWriteTool.setExportType(exportType);
            pdfWriteTool.setId(new Long(paperId).intValue());
            String result = downloadWriteServiceV1.makeWordByPaper(paperId, pdfWriteTool, createWriteTool(), writeTitle(), writeModule(), writeQuestion());
            return result;
        } finally {
            if (null != pdfWriteTool) {
                pdfWriteTool.close();
            }
        }
    }

    @Override
    public String downLoadList(List<Long> paperIds, PaperInfoEnum.TypeInfo typeInfo, QuestionElementEnum.QuestionFieldEnum exportType) {
        return null;
    }

    @Override
    public Map getCacheDown(Long paperId, int paperType, int exportType) {
        String pdfDownCacheInfoKey = RedisKeyConstant.getPdfDownCacheInfo(paperId, paperType, exportType);
        log.info("pdf下载缓存信息:{}", pdfDownCacheInfoKey);
        HashOperations hashOperations = redisTemplate.opsForHash();
        return hashOperations.entries(pdfDownCacheInfoKey);
    }

    @Override
    public void saveDownCache(Long paperId, int paperType, int exportType, Map mapData) {
        String pdfDownCacheInfoKey = RedisKeyConstant.getPdfDownCacheInfo(paperId, paperType, exportType);
        HashOperations hashOperations = redisTemplate.opsForHash();
        hashOperations.putAll(pdfDownCacheInfoKey, mapData);
    }

    @Override
    public void delDownCache(Long paperId, int paperType) {
        Arrays.stream(QuestionElementEnum.QuestionFieldEnum.values()).mapToInt(i -> i.getKey()).forEach(i -> {
            String pdfDownCacheInfoKey = RedisKeyConstant.getPdfDownCacheInfo(paperId, paperType, i);
            log.info("pdf下载缓存信息清除:{}", pdfDownCacheInfoKey);
            redisTemplate.delete(pdfDownCacheInfoKey);
    });
    }

    @Override
    public String downloadByPracticePaper(PracticeCard practiceCard) {
        PdfWriteTool pdfWriteTool = null;
        try {
            pdfWriteTool = new PdfWriteTool();
            pdfWriteTool.setExportType(QuestionElementEnum.QuestionFieldEnum.STEM);
            pdfWriteTool.setAnswerId(practiceCard.getId());
//            BiConsumer<BaseTool, PaperModuleInfo> unWrite = (tool,moduleInfo)->{return;};
            String result = downloadWriteServiceV1.makeWordByPracticeCard(practiceCard, pdfWriteTool, createWriteTool(), writeTitle(), writeModule(), writeQuestion());
//            String result = downloadWriteServiceV1.makeWordByPracticeCard(practiceCard, pdfWriteTool, createWriteTool(), writeTitle(), unWrite, writeQuestion());
            return result;
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != pdfWriteTool) {
                pdfWriteTool.close();
            }
        }
        return null;
    }

    public String handlerQcodeImage(String groundUrl, String waterUrl) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(new URL(groundUrl));
        BufferedImage waterImage = ImageIO.read(new URL(waterUrl));
        // 构建叠加层
        BufferedImage buffImg = NewImageUtils.watermark(bufferedImage, waterImage, 100, 55, 1.0f, 250, 250);;
        String url = uploadImage(buffImg, "png");
        return url;
    }

    private String uploadImage(BufferedImage image, String format) throws IOException {
        @Cleanup
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, format, outputStream);
        String fileName = UUID.randomUUID().toString() + "." + format;
        InputStream inputStream = null;
        try {
            inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            UploadFileUtil.getInstance().ftpUploadFileInputStream(inputStream, fileName, UploadFileUtil.IMG_PATH_QUESTION);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            inputStream.close();
        }
        return UploadFileUtil.IMG_URL_QUESTION + fileName;
    }

    /**
     * 写入试题信息
     *
     * @return
     */
    public BiConsumer<BaseTool, Map<String, Object>> writeQuestion() {
        return ((baseTool, questionMap) -> {
            if (baseTool instanceof PdfWriteTool) {
                try {
                    addQuestionElement((PdfWriteTool) baseTool,
                            questionMap,
                            ((PdfWriteTool) baseTool).getExportType());
                } catch (Exception e) {
                    log.error(e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }


    public static BiConsumer<BaseTool, Object> addText() {
        return (BaseTool baseTool, Object o) -> {
            if (baseTool instanceof PdfWriteTool) {
                if (o instanceof java.util.List) {
                    //一个含有图片的段落
                    for (Object obj : (List) o) {
                        PdfUtil.handerElement((PdfWriteServiceImplV1.PdfWriteTool) baseTool, obj);
                    }
                } else {
                    //一个出文本图片处理
                    PdfUtil.handerElement((PdfWriteServiceImplV1.PdfWriteTool) baseTool, o);
                }
                PdfUtil.addContentAndImage((PdfWriteServiceImplV1.PdfWriteTool) baseTool);
            }
        };
    }

    private void addQuestionElement(PdfWriteTool baseTool, Map<String, Object> questionMap, QuestionElementEnum.QuestionFieldEnum exportType) throws Exception {
        int type = Integer.parseInt(questionMap.getOrDefault("type", "-1").toString());
        if (type < 0) {
            log.error("题型属性不具备：map={}", questionMap);
            return;
        }
        QuestionInfoEnum.QuestionSaveTypeEnum questionSaveTypeEnum = QuestionInfoEnum.getSaveTypeByQuestionType(type);
        QuestionElementEnum.QuestionOperateEnum questionOperateEnum = QuestionElementEnum.QuestionOperateEnum.create(questionSaveTypeEnum);
        //元素操作
        List<QuestionElementEnum.ElementEnum> values = questionOperateEnum.getValue();
        for (QuestionElementEnum.ElementEnum element : values) {
            if (element.getPdfLocation().equals(QuestionElementEnum.LocationEnum.UN_KNOW)) {
                continue;
            }
            //只处理符合类型的标签
            if (exportType.equals(QuestionElementEnum.QuestionFieldEnum.COMMON) ||
                    element.getField().equals(QuestionElementEnum.QuestionFieldEnum.COMMON) ||
                    element.getField().equals(exportType)) {
                String value = String.valueOf(questionMap.get(element.getKey()));
                if ("null".equals(value) || StringUtils.isBlank(value)) {
                    continue;
                }
                if (element.equals(QuestionElementEnum.ElementEnum.TYPE)) {
                    value = EnumUtil.valueOf(Integer.parseInt(value), QuestionInfoEnum.QuestionTypeEnum.class);
                }
                if (element.getPdfLocation().equals(QuestionElementEnum.LocationEnum.OLD_LINE)) {
                    CommonFileUtil.fillContent(element.getName() + value, baseTool, addText());
                    PdfUtil.addElements(baseTool.getElementList(), baseTool);
                    continue;
                } else if (element.getPdfLocation().equals(QuestionElementEnum.LocationEnum.NEW_LINE)) {
                    CommonFileUtil.fillContent(element.getName(), baseTool, addText());
                    CommonFileUtil.fillContent(value, baseTool, addText());
                    PdfUtil.addElements(baseTool.getElementList(), baseTool);
                    continue;
                }
                //其他自定义的展示情况
                if (element.equals(QuestionElementEnum.ElementEnum.STEM)) {

                    String sort = String.valueOf(questionMap.getOrDefault(QuestionElementEnum.ElementEnum.SORT.getKey(), "-1"));
                    if (!"-1".equals(sort)) {
                        sort = sort + ".";
                        CommonFileUtil.fileExistsImg(sort, value, baseTool, addText());
                        baseTool.setIndentation((int) PDFDocument.getFontLength(sort, baseTool.getQuestionFont()));
                        PdfUtil.addElements(baseTool.getElementList(), baseTool);
                    } else {
                        CommonFileUtil.fillContent(value, baseTool, addText());
                        PdfUtil.addElements(baseTool.getElementList(), baseTool);
                    }
                    continue;
                }
                if (element.equals(QuestionElementEnum.ElementEnum.CHOICE)) {
                    List<String> choices = HtmlConvertUtil.parseChoices(value);
                    for (int i = 0; i < choices.size(); i++) {
                        char perChar = (char) ('A' + i);
                        CommonFileUtil.fileExistsImg(perChar + element.getName(), choices.get(i), baseTool, addText());
                    }
                    PDFDocument.handleChoice(baseTool.getElementList(), choices, baseTool.getDocument());
                    baseTool.getElementList().clear();
                    continue;
                }
                if (element.equals(QuestionElementEnum.ElementEnum.MATERIAL)) {
                    String[] split = value.split(element.getName());
//                    CommonFileUtil.fillContent(element.getName(), baseTool, addText());
                    documentAddElement(baseTool.getDocument(),new Paragraph("\n"));//资料前加一个空段落作分割
                    for (String s : split) {
                        String[] split1 = s.split("<[/]?br[/]?>");
                        for (String s1 : split1) {
                            CommonFileUtil.fileExistsImg("", s1.trim(), baseTool, addText());
                            baseTool.setIndentation(-(int) PDFDocument.getFontLength("资料", baseTool.getQuestionFont()));
                            PdfUtil.addElements(baseTool.getElementList(), baseTool);
                        }
                    }
                }
            }
        }

    }


//    public BiConsumer<BaseTool, List<String>> addStringText() {
//        return ((baseTool, strings) -> {
//            if (baseTool instanceof PdfWriteTool) {
//                Document document = ((PdfWriteTool) baseTool).getDocument();
//                Font questionFont = ((PdfWriteTool) baseTool).getQuestionFont();
//                //TODO实现字符串集合加入段落
//                if (strings.size() == 1) {
//                    Paragraph paragraph = new Paragraph(strings.get(0), questionFont);
//                    //设定段落间距  paragraph.setLeading();
//                    documentAddElement(document, paragraph);
//                } else {
//                    Paragraph paragraph = new Paragraph("", questionFont);
//                    for (int i = 0; i < strings.size(); i++) {
//                        Phrase phrase = new Phrase(strings.get(i), questionFont);
//                        paragraph.add(phrase);
//                        if (i < strings.size() - 1) {
//                            paragraph.add("\t");
//                        }
//                    }
//                    documentAddElement(document, paragraph);
//                }
//            }
//        });
//    }

    private void documentAddElement(Document document, Element element) {
        try {
            document.add(element);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }


    /**
     * 添加模块
     *
     * @return
     */
    public BiConsumer<BaseTool, PaperModuleInfo> writeModule() {
        return ((baseTool, paperModuleInfo) -> {
            if (baseTool instanceof PdfWriteTool) {
                try {
                    addModuleElement(((PdfWriteTool) baseTool).getDocument(), ((PdfWriteTool) baseTool).getModuleFont(), paperModuleInfo.getName(), paperModuleInfo.getId());
                } catch (DocumentException e) {
                    log.error(e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void addModuleElement(Document document, Font moduleFont, String name, Integer id) throws DocumentException {
        document.add(Chunk.NEWLINE);
        Paragraph paragraph = new Paragraph(name, moduleFont);
        paragraph.setLeading(moduleFont.getSize());
        paragraph.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(paragraph);
        document.add(Chunk.NEWLINE);
    }

    /**
     * 写入标题+二维码
     *
     * @return
     */
    public BiConsumer<BaseTool, String> writeTitle() {
        return (BaseTool baseTool, String title) -> {
            if (baseTool instanceof PdfWriteTool) {
                PdfWriteTool pdfWriteTool = (PdfWriteTool) baseTool;
                Document document = pdfWriteTool.getDocument();
                Font font = pdfWriteTool.getTitleFont();
                PdfWriter writer = pdfWriteTool.getRtfWriter2();
                float y = document.top() - 100;      //纵坐标
                PdfUtil.writeAlignCenter(title, y, writer, font);
                document.newPage();
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
            PdfWriteTool pdfWriteTool = null;
            /**
             * 临时文件名称加时间戳，防止并发情况导致多线程处理文件内容导致异常
             */
            String namePath = name.replace("/", "") + System.currentTimeMillis();
            try {
                if (null == tool) {
                    pdfWriteTool = new PdfWriteTool();
                }
                if (tool instanceof PdfWriteTool) {
                    pdfWriteTool = (PdfWriteTool) tool;
                    File file = new File(pdfWriteTool.getDir() + namePath + pdfWriteTool.getSuffix());
//                    Document document = new Document(PageSize.A4, 36, 36, 40, 40);
                    Document document = new Document(PageSize.A4, 50, 50, 100, 55);
                    pdfWriteTool.setDocument(document);
                    pdfWriteTool.setFile(file);
                    PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
                    pdfWriteTool.setRtfWriter2(writer);
                    String headCode = "http://tiku.huatu.com/cdn/essay/common/downloadAPP.png";
                    System.out.println("headCode = " + headCode);

                    String url = "http://tiku.huatu.com/cdn/pandora/img/answer-qcode-back-ground-0.png";
                    //生成二维码
                    String tailCode = handlerQcodeImage(url, CourseQCode.getInstance().uploadQCodeImgAndReturnPath(new Long(pdfWriteTool.getId())));

                    if(pdfWriteTool.getAnswerId()>0){
                        tailCode = handlerQcodeImage(tailCode, CourseQCode.getInstance().uploadQCodeImgAndReturnPath(pdfWriteTool.getAnswerId(),CourseQCode.ENABLE_ANSWER_CARD_URL));
                    }else{
                        tailCode = handlerQcodeImage(tailCode, CourseQCode.getInstance().uploadQCodeImgAndReturnPath(new Long(pdfWriteTool.getId())));
                    }
                    int lenght = Math.min(30, name.length());
                    if (name.length() > lenght) {
                        name = name.substring(0, lenght) + "...";
                    }
                    PDFHeaderFooter header = new PDFHeaderFooter(name, headCode, tailCode, pdfWriteTool.getBaseFont());
                    writer.setBoxSize("art", PageSize.A4);
                    writer.setPageEvent(header);
                    //打开文件
                    document.open();
                }
                return pdfWriteTool;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (DocumentException e) {
                e.printStackTrace();
            } catch (WriterException e) {
                e.printStackTrace();
            }
            return pdfWriteTool;

        });
        return function;
    }

    @Data
    public static class PdfWriteTool extends BaseTool {
        private Document document;
        private PdfWriter rtfWriter2;

        private LinkedList<ElementInfo> elementList = Lists.newLinkedList();   //以结构相似的元素为组存储
        private LinkedList<PdfElement> elements = Lists.newLinkedList();        //以phrse和image元素存储
        private BaseFont bfChinese;
        //        private final BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        private Font titleFont;
        private Font moduleFont;
        private Font questionFont;
        private Font baseFont;
        /**
         * =-等特殊符号的最小高度限定
         */
        private float specialHeight = 5f;
        /**
         * 表达式类的图片缩小比例
         */
        private float imagePercent = 0.56f;
        /**
         * 小图和大图的限定值
         */
        private float maxHeight = 40f * imagePercent;
        /**
         * 大图和换行大图直接的限定值
         */
        private float maxBigHeight = 80f * imagePercent;


        public PdfWriteTool() throws IOException, DocumentException {
            super(FunFileUtils.TMP_PDF_SOURCE_FILEPATH, PDF_TAIL_NAME, null, 0, 1,-1, 0, "", false, null, null, true);
            try {
                File file = new File("C:/WINDOWS/Fonts/simsun.ttf");
                if (file.exists()) {
                    bfChinese = BaseFont.createFont("C:/WINDOWS/Fonts/simsun.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                } else {
                    bfChinese = BaseFont.createFont("/usr/share/fonts/simsun.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                }

            } catch (Exception e) {
                bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            }
            titleFont = new Font(bfChinese, 21, Font.BOLD);
            moduleFont = new Font(bfChinese, 18, Font.BOLD);
            questionFont = new Font(bfChinese, 11, Font.NORMAL);
            baseFont = new Font(bfChinese, 12, Font.NORMAL);
        }

        public void close() {

            //添加尾页
            if (null != rtfWriter2 && null != document && document.isOpen()) {
                PDFHeaderFooter pageEvent = (PDFHeaderFooter) this.getRtfWriter2().getPageEvent();
                pageEvent.setFootFlag(this.isFooterFlag());
                if(this.isFooterFlag()){
                    document.newPage();
                    PdfUtil.writeTitle(this, "查看参考答案与详细解析");
                    int number = getRtfWriter2().getPageNumber();
                    pageEvent.setPageNum(number);
                    pageEvent.writeTailPage(getRtfWriter2(),document,this);
                }
            }
            if (null != document && document.isOpen()) {
                document.close();
            }
            if (null != rtfWriter2) {
                rtfWriter2.close();
            }
            if (null != getFile() && getFile().exists()) {
//                System.out.println("getFile().getAbsolutePath() = " + getFile().getAbsolutePath());
                getFile().deleteOnExit();
            }
        }

        public void addElement(PdfElement element) {
            if (element.getWidth() > 0f) {
                elements.addLast(element);
            }
        }

        public void addElementInfo(ElementInfo elementInfo) {
            elementList.addLast(elementInfo);
        }

    }

    @Data
    @NoArgsConstructor
    public static class ElementInfo {
        private ArrayList<PdfElement> elements = Lists.newArrayList();
        private float maxHeight = 0;

        public boolean addAll(List<PdfElement> list, float height) {
            elements.addAll(list);
            if (maxHeight / height > 2 || height / maxHeight > 2) {
                maxHeight = Math.max(maxHeight, height);
                return false;
            } else {
                maxHeight = Math.max(maxHeight, height);
                return true;
            }

        }
    }
}
