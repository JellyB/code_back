package com.huatu.tiku.teacher.util.file;

import com.alibaba.druid.util.StringUtils;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.entity.download.BaseTool;
import com.huatu.tiku.teacher.service.impl.download.v1.PdfWriteServiceImplV1;
import com.huatu.tiku.teacher.util.ChineseSplitCharacter;
import com.huatu.tiku.util.file.FunFileUtils;
import com.huatu.tiku.util.file.UploadFileUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by huangqingpeng on 2018/11/15.
 */
public class PdfUtil {

    private static final Logger logger = LoggerFactory.getLogger(PdfUtil.class);

    @AllArgsConstructor
    @Getter
    public enum PdfTailQCode {
        XINGCE("行测", UploadFileUtil.IMG_URL + "answer-qcode-back-ground-1.png", 624, 348),
        Other("其他", UploadFileUtil.IMG_URL + "answer-qcode-back-ground-2.png", 834, 397);
        private String name;
        private String url;
        private int width;
        private int height;
    }

    public static void testQuestion(PdfWriteServiceImplV1.PdfWriteTool pdfWriteTool) {
        String stem = "<p>下面哪一张图能正确反<img src=\"http://tiku.huatu.com/cdn/pandora/img/question/5bc289d6-401e-4b56-a68d-977e27f45ee9.png\" style=\"width: 39;height: 17;\" width=\"39\" height=\"17\">映2011~2015年民政<img src=\"http://tiku.huatu.com/cdn/pandora/img/question/ed43fb28-47fd-47f8-84eb-4ce040d9d25d.png\" style=\"width: 13;height: 23;\" width=\"13\" height=\"23\">" +
                "部门接收捐赠衣被数量的变化？<br/>       " +
                "<img src=\"http://tiku.huatu.com/cdn/images/vhuatu/tiku/v/vn1WdQGE4QUrRiYUw5_ldlyHF0A.png\" width=\"115\" height=\"85\" style=\"width:115;height:85\">" +
                "<img src=\"http://tiku.huatu.com/cdn/images/vhuatu/tiku/s/sMQFEWkDJF8kzHuTvOZtHX9TvS_.png\" width=\"118\" height=\"88\" style=\"width:118;height:88\">" +
                "<img src=\"http://tiku.huatu.com/cdn/images/vhuatu/tiku/u/uZQYVq93SFGXYOCo8QKrp6dtxAp.png\" width=\"124\" height=\"89\" style=\"width:124;height:89\">" +
                "<img src=\"http://tiku.huatu.com/cdn/images/vhuatu/tiku/r/rKtfH_KjLZc5kCeBZEDVEM132Ym.png\" width=\"126\" height=\"85\" style=\"width:126;height:85\"></p>";
        try {
            CommonFileUtil.fillContent(stem, pdfWriteTool, addText());
            addElements(pdfWriteTool.getElementList(), pdfWriteTool);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 图片和文字按照是否需要换行处理分成不同的元素组合
     *
     * @param pdfWriteTool
     */
    public static void addContentAndImage(PdfWriteServiceImplV1.PdfWriteTool pdfWriteTool) {
        LinkedList<PdfElement> elements = pdfWriteTool.getElements();
        List<PdfElement> tempElements = Lists.newArrayList();//临时list，存储某一片段的element
        /**
         * 需要换行的图片跟不需要换行的图片区分处理
         */
        boolean flag = false;
        float maxBigHeight = pdfWriteTool.getMaxBigHeight();
        while (true) {
            if (CollectionUtils.isEmpty(elements)) {
                break;
            }
            PdfElement first = elements.removeFirst();
            if (CollectionUtils.isEmpty(tempElements)) {        //首次插入的时候，不做判断
                flag = first.getHeight() > maxBigHeight;
                tempElements.add(first);
                continue;
            }
            boolean currentFlag = first.getHeight() > maxBigHeight; //当前元素是否是大图的标识
            if (flag != currentFlag) {  //与上一个元素标识不同时，开始分组操作
                //将临时list中的数据组成新的list，再清空临时缓存,最后塞入本次的数据
                ArrayList<PdfElement> pdfElements = Lists.newArrayList(tempElements);
                PdfWriteServiceImplV1.ElementInfo elementInfo = new PdfWriteServiceImplV1.ElementInfo();
                elementInfo.addAll(pdfElements, pdfElements.stream().map(PdfElement::getHeight).max(Float::compareTo).get());
                pdfWriteTool.addElementInfo(elementInfo);   //将前面的元素打包存入tool里
                tempElements.clear();
                tempElements.add(first);
                flag = currentFlag;
                continue;
            }
            tempElements.add(first);    //与上一个元素标识是一致的
        }
        if (CollectionUtils.isNotEmpty(tempElements)) {
            PdfWriteServiceImplV1.ElementInfo elementInfo = new PdfWriteServiceImplV1.ElementInfo();
            elementInfo.addAll(tempElements, tempElements.stream().map(PdfElement::getHeight).max(Float::compareTo).get());
            pdfWriteTool.addElementInfo(elementInfo);
        }
    }


    /**
     * 对分组的后的元素集合做写入操作
     * 数据源： 一段文本，包含图片存在的可能
     *
     * @param list         多个分组数据组成一段内容
     * @param pdfWriteTool
     * @throws DocumentException
     */
    public static void addElements(LinkedList<PdfWriteServiceImplV1.ElementInfo> list, PdfWriteServiceImplV1.PdfWriteTool pdfWriteTool) throws DocumentException {
        LinkedList<PdfWriteServiceImplV1.ElementInfo> tempList = Lists.newLinkedList();
        while (true) {
            if (CollectionUtils.isEmpty(list)) {
                break;
            }
            PdfWriteServiceImplV1.ElementInfo first = list.removeFirst();
            for (PdfElement pdfElement : first.getElements()) {
                Element element = pdfElement.getElement();
                if (element instanceof Phrase) {
                    String content = ((Phrase) element).getContent();
                    /**
                     * 分段处理逻辑
                     */
                    if (content.indexOf("\n") > -1) {
                        first = SplitElementInfo(tempList, first, pdfElement, content.indexOf("\n"), pdfWriteTool);
                    }
                }
            }
            if (null != first) {
                tempList.addLast(first);
            }
        }
        addSingleParagh(tempList, pdfWriteTool);
    }

    /**
     * 将first拆分成两个elementInfo,左边的塞到templist,并将templist写入，之后剩下的右边部分重新构成first
     *
     * @param tempList     临时存储下一次需要写入的数据
     * @param first        拆分对象
     * @param pdfElement   含有\n的元素
     * @param startIndex   \n在元素中的位置
     * @param pdfWriteTool
     */
    private static PdfWriteServiceImplV1.ElementInfo SplitElementInfo(LinkedList<PdfWriteServiceImplV1.ElementInfo> tempList, PdfWriteServiceImplV1.ElementInfo first, PdfElement pdfElement, int startIndex, PdfWriteServiceImplV1.PdfWriteTool pdfWriteTool) {
        ArrayList<PdfElement> elements = first.getElements();
        int index = elements.indexOf(pdfElement);

        if (index == elements.size() - 1) { //如果是分组中的最后一个元素，则返回null
            tempList.addLast(first);
            addSingleParagh(tempList, pdfWriteTool);
            tempList.clear();
            return null;
        }
        PdfWriteServiceImplV1.ElementInfo leftElementInfo = new PdfWriteServiceImplV1.ElementInfo();
        PdfWriteServiceImplV1.ElementInfo rightElementInfo = new PdfWriteServiceImplV1.ElementInfo();
        List<PdfElement> left = Lists.newArrayList(); //左半边数据
        if (index > 0) {
            left.addAll(elements.subList(0, index));
        }
        Element element = pdfElement.getElement();
        List<PdfElement> right = Lists.newArrayList();
        if (element instanceof Phrase) {
            String content = ((Phrase) element).getContent();
            PdfElement splitLeft = new PdfElement(new Phrase(content.substring(0, startIndex) + "\n", pdfWriteTool.getQuestionFont()));
            PdfElement splitRight = new PdfElement(new Phrase(content.substring(startIndex).replaceFirst("\n", ""), pdfWriteTool.getQuestionFont()));
            left.add(splitLeft);
            right.add(splitRight);
        }
        right.addAll(elements.subList(index + 1, elements.size()));  //右半边数据
        leftElementInfo.addAll(left, left.stream().map(PdfElement::getHeight).max(Float::compareTo).get());
        tempList.addLast(leftElementInfo);
        addSingleParagh(tempList, pdfWriteTool);
        tempList.clear();
        rightElementInfo.addAll(right, right.stream().map(PdfElement::getHeight).max(Float::compareTo).get());
        return rightElementInfo;
    }

    private static void addSingleParagh(LinkedList<PdfWriteServiceImplV1.ElementInfo> list, PdfWriteServiceImplV1.PdfWriteTool pdfWriteTool) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        Document document = pdfWriteTool.getDocument();
        boolean flag = true;
        while (flag) {
            if (CollectionUtils.isEmpty(list)) {
                break;
            }
            PdfWriteServiceImplV1.ElementInfo elementInfo = list.removeFirst();
            float height = elementInfo.getMaxHeight();
            //全是图片的情况，需要换行
            if (height > pdfWriteTool.getMaxBigHeight()) {     //大图片组合处理（一个大图片或者多个大图片）
                try {
                    if (elementInfo.getElements().size() == 1) {
                        //做居中处理(单个大图片)
                        PDFDocument.handleImageList(elementInfo.getElements().get(0), document, Image.MIDDLE);
                    } else if (elementInfo.getElements().size() > 1 && elementInfo.getElements().get(0).getWidth() > 250) {  //多个元素，第一种是全是宽图片
                        for (PdfElement pdfElement : elementInfo.getElements()) {
                            PDFDocument.handleImageList(pdfElement, document, Image.MIDDLE);
                        }
                    } else {  //多个元素，存在窄图片的情况
                        PDFDocument.handleImageList(elementInfo, document);
                    }
                } catch (DocumentException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                continue;
            }
            //不换行写入图片的情况细分：1需要表格矫正图片居中的，2正常段落写入，不用矫正图片居中格式
            float maxHeight = pdfWriteTool.getMaxHeight();
            ArrayList<PdfElement> elements = elementInfo.getElements();
            if (CollectionUtils.isEmpty(elements)) {
                continue;
            }
            Optional<Float> max = elements.stream().filter(i -> i.getHeight() > 0).map(PdfElement::getHeight).max(Float::compareTo);
            Float tempHeight = pdfWriteTool.getQuestionFont().getSize();
            if (max.isPresent()) {
                tempHeight = max.get();
            }
            if (tempHeight > maxHeight) {     //1需要表格矫正图片居中的
                PDFDocument.handleImageParagraph(elements, document, maxHeight);
                continue;
            }
            Paragraph paragraph = new Paragraph("", pdfWriteTool.getQuestionFont());
            if (pdfWriteTool.getIndentation() != 0) {
                paragraph.setFirstLineIndent(-pdfWriteTool.getIndentation());
                pdfWriteTool.setIndentation(0); //恢复段落缩进的尺寸
            }
            //处理不用换行的图文混排（2正常段落写入，不用矫正图片居中格式）
            for (PdfElement pdfElement : elements) {
                Element element = pdfElement.getElement();
                if (element instanceof Phrase) {
                    Phrase phrase = ((Phrase) element);
                    String content = phrase.getContent();
                    Chunk chunk = instantiateChunk(content, phrase.getFont());
                    paragraph.add(chunk);
                } else if (element instanceof Chunk) {
                    Image image = ((Chunk) element).getImage();
                    /**
                     * 下划线形式的内容
                     */
                    if (null == image) {
                        paragraph.add((Chunk) element);
                        continue;
                    }
                    image.scaleToFit(pdfElement.getWidth(), pdfElement.getHeight());
                    paragraph.add(new Chunk(image, 0, (11 - pdfElement.getHeight()) / 2));
                }
            }

            try {
                document.add(paragraph);
            } catch (DocumentException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 检查图片大小，初始化图片大小
     *
     * @param
     * @param url
     * @param widthString
     * @param heightString @return
     */
    private static PdfElement checkSize(String url, PdfWriteServiceImplV1.PdfWriteTool pdfWriteTool, String widthString, String heightString) throws IOException, BadElementException {
        //logger.info("pdf待处理url是:{}", url);
        Stopwatch stopwatch = Stopwatch.createStarted();
        Chunk chunk = null;
        PdfElement pdfElement = null;
        float height = 0f;
        System.out.println("Image.getInstance :" + url);
        Image image = Image.getInstance(url);
        Document document = pdfWriteTool.getDocument();
//        float maxWidth = (document.right() - document.left()) * 0.4f;
//        float maxHeight = maxWidth * 0.8f;
        float imageHeight = image.getHeight();
        float imageWidth = image.getWidth();
        if (StringUtils.isNumber(heightString)) {
            imageHeight = Float.parseFloat(heightString);
        }
        if (StringUtils.isNumber(widthString)) {
            imageWidth = Float.parseFloat(widthString);
        }
        float fontSize = pdfWriteTool.getQuestionFont().getSize();
        float imagePercent = pdfWriteTool.getImagePercent();
        if (imageHeight < pdfWriteTool.getSpecialHeight()) {
            image.scaleToFit(imageWidth, imageHeight);
            image.setAlignment(Image.LEFT | Image.TEXTWRAP);
            chunk = new Chunk(image, 0f, (fontSize - imageHeight) / 2f);
            pdfElement = new PdfElement(chunk, imageWidth, imageHeight, url);
        } else if (imageHeight < 20) {
//            height = imageHeight * pdfWriteTool.getImagePercent();
            image.scaleToFit(imageWidth * imagePercent, imageHeight * imagePercent);
            image.setAlignment(Image.LEFT | Image.TEXTWRAP);
            chunk = new Chunk(image, 0f, -0f);
            pdfElement = new PdfElement(chunk, imageWidth * imagePercent, imageHeight * imagePercent, url);
        } else if (imageHeight < 80) {
            height = imageHeight * pdfWriteTool.getImagePercent();
            float width = imageWidth * pdfWriteTool.getImagePercent();
            image.scaleToFit(width, height);
            image.setAlignment(Image.LEFT | Image.TEXTWRAP);
            chunk = new Chunk(image, 0f, (fontSize - height) / 2f);
            pdfElement = new PdfElement(chunk, width, height, url);
        }
        imageHeight = imageHeight * 0.4f;
        imageWidth = imageWidth * 0.4f;
//        if (imageWidth > maxWidth) {        //大图判断
//            float width = 0;
//            height = maxWidth / imageWidth * imageHeight;
//            if (maxHeight < height) {       //如果图片是高图片而非扁图片(二次缩放图片)
//                height = maxHeight;
//                width =imageWidth  * maxHeight / imageHeight;
//            } else {
//                width = maxWidth;
//            }
//            image.scaleToFit(width, height);
//            image.setAlignment(Image.MIDDLE | Image.TEXTWRAP);
//            chunk = new Chunk(image, 0f, -height + 8f);
//            pdfElement = new PdfElement(chunk, width, height, url);
//        }

        if (null == chunk) {
            image.setAlignment(Image.MIDDLE | Image.TEXTWRAP);
            height = imageHeight;
            image.scaleToFit(imageWidth, height);
            chunk = new Chunk(image, 0f, -height + 8f);
            pdfElement = new PdfElement(chunk, imageWidth, height, url);
        }
        //logger.info("完成pdf处理url是:{},耗时:{}", url, String.valueOf(stopwatch.stop()));
        return pdfElement;
    }

    public static void writeTitle(PdfWriteServiceImplV1.PdfWriteTool pdfWriteTool, String title) {
        PdfWriter writer = pdfWriteTool.getRtfWriter2();
        Document document = pdfWriteTool.getDocument();
        // 汉字处理
        Font titleFont = pdfWriteTool.getTitleFont();
        // 标题展示
        float y = document.top() - 120;      //纵坐标
        writeAlignCenter(title, y, writer, titleFont);
    }

    public static void writeAlignCenter(String title, Float y, PdfWriter writer, Font font) {
        int index = 0;
        float startY = y;
        while (index < title.length()) {
            int end = Math.min(index + 25, title.length());
            String temp = title.substring(index, end);
            Phrase phrase = new Phrase(temp, font);
            ColumnText.showTextAligned(writer.getDirectContent(),
                    Element.ALIGN_CENTER, phrase,
                    PageSize.A4.getWidth() / 2, startY, 0);
            index = end;
            System.out.println("title.y = " + startY);
            startY = startY - font.getSize() - phrase.getLeading();
        }
    }

    public static PdfWriteServiceImplV1.PdfWriteTool initCreateTool() {
        PdfWriteServiceImplV1.PdfWriteTool pdfWriteTool = null;
        try {
            pdfWriteTool = new PdfWriteServiceImplV1.PdfWriteTool();
            File file = new File(pdfWriteTool.getDir() + "test" + pdfWriteTool.getSuffix());
            System.out.println("file = " + file.getAbsolutePath());
            Document document = new Document(PageSize.A4, 50, 50, 100, 55);
            pdfWriteTool.setDocument(document);
            pdfWriteTool.setFile(file);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
            pdfWriteTool.setRtfWriter2(writer);
            String url = "http://tiku.huatu.com/cdn/pandora/img/answer-qcode-back-ground-0.png";
            PDFHeaderFooter header = new PDFHeaderFooter("试卷名称", FunFileUtils.PDF_FILE_SAVE_URL + "question-qcode-back-ground.png",
                    url,
                    pdfWriteTool.getBaseFont());

            writer.setBoxSize("art", PageSize.A4);

            writer.setPageEvent(header);
            writer.setFullCompression();
            document.open();
            //打开文件


        } catch (IOException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return pdfWriteTool;
    }

    /**
     * 将需要插入的数据插入到临时list变量中
     *
     * @return
     */
    public static BiConsumer<BaseTool, Object> addText() {
        return (BaseTool baseTool, Object o) -> {
            if (baseTool instanceof PdfWriteServiceImplV1.PdfWriteTool) {
                if (o instanceof java.util.List) {
                    ((List) o).stream().forEach(i -> handerElement((PdfWriteServiceImplV1.PdfWriteTool) baseTool, i));
                } else {
                    handerElement((PdfWriteServiceImplV1.PdfWriteTool) baseTool, o);
                }
                addContentAndImage((PdfWriteServiceImplV1.PdfWriteTool) baseTool);
            }
        };
    }

    /**
     * 解析通用元素为pdf元素
     *
     * @param baseTool
     * @param obj
     */
    public static void handerElement(PdfWriteServiceImplV1.PdfWriteTool baseTool, Object obj) {
        if (obj instanceof String) {
            String content = String.valueOf(obj);
            System.out.println("content = " + content);
            if (org.apache.commons.lang3.StringUtils.isNotBlank(content)) {
                handlerTextWithUnderLineAndBold(content, baseTool);
            }
        } else if (obj instanceof HashMap) {
            String imgTag = MapUtils.getString((Map) obj, "imgTag");
            try {
                String url = CommonFileUtil.subAttrString(imgTag, "src");
                Map<String, String> map = handlerImageSize(imgTag);
                PdfElement pdfElement = checkSize(url, baseTool, MapUtils.getString(map, "widthString"), MapUtils.getString(map, "heightString"));
                baseTool.addElement(pdfElement);
            } catch (BizException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (BadElementException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 处理含有下滑线和加粗字体的文本内容
     *
     * @param content
     * @param baseTool
     */
    private static void handlerTextWithUnderLineAndBold(String content, PdfWriteServiceImplV1.PdfWriteTool baseTool) {
        Pattern pattern = Pattern.compile("<u>([^(</u>)]+)</u>");
        Matcher matcher = pattern.matcher(content);
        int index = 0;
        while (matcher.find(index)) {
            int start = matcher.start();
            if (start > index) {
//                baseTool.addElement(new PdfElement(new Phrase(content.substring(index,start),baseTool.getQuestionFont())));
                handlerTextWithStrong(baseTool, content.substring(index, start), baseTool.getQuestionFont());
            }
//            Chunk underline = getUnderLineChunk(matcher.group(1),baseTool.getQuestionFont());
//            baseTool.addElement(new PdfElement(underline));
            handlerUnderLineTextWithStrong(baseTool, matcher.group(1), baseTool.getQuestionFont());
            index = matcher.end();
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(content.substring(index))) {
//            baseTool.addElement(new PdfElement(new Phrase(content.substring(index),baseTool.getQuestionFont())));
            handlerTextWithStrong(baseTool, content.substring(index), baseTool.getQuestionFont());
        }
    }

    /**
     * 处理带有下划线的加粗字体
     *
     * @param baseTool
     * @param content
     * @param questionFont
     */
    private static void handlerUnderLineTextWithStrong(PdfWriteServiceImplV1.PdfWriteTool baseTool, String content, Font questionFont) {
        Pattern pattern = Pattern.compile("<strong>([^<]+)</strong>");
        Matcher matcher = pattern.matcher(content);
        Font boldFont = new Font(questionFont.getBaseFont(), questionFont.getSize(), Font.BOLD);
        int i = 0;
        while (matcher.find(i)) {
            int start = matcher.start();
            int end = matcher.end();
            if (i < start) {
                Chunk underline = getUnderLineChunk(content.substring(i, start), questionFont);
                baseTool.addElement(new PdfElement(underline));
            }
            String group = matcher.group(1);
            Chunk underline = getUnderLineChunk(group, boldFont);
            baseTool.addElement(new PdfElement(underline));
            i = end;
        }
        if (content.length() > i) {
            Chunk underline = getUnderLineChunk(content.substring(i, content.length()), questionFont);
            baseTool.addElement(new PdfElement(underline));
        }
    }

    /**
     * 处理值含有加粗特性的内容
     *
     * @param baseTool
     * @param content
     * @param questionFont
     */
    private static void handlerTextWithStrong(PdfWriteServiceImplV1.PdfWriteTool baseTool, String content, Font questionFont) {
        Pattern pattern = Pattern.compile("<strong>([^<]+)</strong>");
        Matcher matcher = pattern.matcher(content);
        Font boldFont = new Font(questionFont.getBaseFont(), questionFont.getSize(), Font.BOLD);
        int i = 0;
        while (matcher.find(i)) {
            int start = matcher.start();
            int end = matcher.end();
            if (i < start) {
                baseTool.addElement(new PdfElement(new Phrase(content.substring(i, start), questionFont)));
            }
            String group = matcher.group(1);
            baseTool.addElement(new PdfElement(new Phrase(group, boldFont)));
            i = end;
        }
        if (content.length() > i) {
            baseTool.addElement(new PdfElement(new Phrase(content.substring(i, content.length()), questionFont)));
        }
    }

    public static Map<String, String> handlerImageSize(String imgTag) throws BizException {
        String widthString = "";
        String heightString = "";
        if (imgTag.indexOf("style") > 0) {
            //style=\"width: 221;height: 169;\"
            widthString = CommonFileUtil.getSizeFromStyle("width", imgTag).replace("px", "").trim();
            heightString = CommonFileUtil.getSizeFromStyle("height", imgTag).replace("px", "").trim();
        } else {
            // width=\"221\" height=\"169\"
            widthString = CommonFileUtil.subAttrString(imgTag, "width");
            heightString = CommonFileUtil.subAttrString(imgTag, "height");
        }
        HashMap<String, String> map = Maps.newHashMap();
        map.put("widthString", widthString);
        map.put("heightString", heightString);
        return map;
    }

    /**
     * 内容添加下划线
     *
     * @param content
     * @param questionFont
     * @return
     */
    public static Chunk getUnderLineChunk(String content, Font questionFont) {
        Chunk underline = instantiateChunk(content, questionFont);
        underline.setUnderline(0.1f, -1f);
        return underline;
    }

    /**
     * Chunk字符串内容实例化
     *
     * @return
     */
    public static Chunk instantiateChunk(String content, Font font) {
        Chunk chunk = new Chunk(content, font);
        //中文内容标点出现在行首的处理方案
        chunk.setSplitCharacter(ChineseSplitCharacter.SplitCharacter);
        return chunk;
    }
}
