package com.huatu.tiku.essay.util.file;


import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.huatu.tiku.essay.entity.EssayMockExam;
import com.huatu.tiku.essay.essayEnum.YesNoEnum;
import com.huatu.tiku.essay.repository.EssayUserAnswerQuestionDetailedScoreRepository;
import com.huatu.tiku.essay.util.admin.EssayConvertUtil;
import com.huatu.tiku.essay.vo.file.TagPosition;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

;

/**
 * Create by jbzm on 171212
 */
@Slf4j
public class PdfUtil {


    @Autowired
    EssayUserAnswerQuestionDetailedScoreRepository essayUserAnswerQuestionDetailedScoreRepository;


    public static final BaseFont BASEFONT = getBaseFont();
    public static final Font COVER_TITLE_FONT = new Font(BASEFONT, 21, Font.BOLD);
    public static final Font TITLE_FONT = new Font(BASEFONT, 13, Font.BOLD);
    public static final Font LITTLE_TITLE_FONT = new Font(BASEFONT, 12, Font.NORMAL);
    public static final Font CONTENT_TITILE_FONT = new Font(BASEFONT, 11, Font.BOLD);
    public static final Font CONTENT_FONT = new Font(BASEFONT, 11, Font.NORMAL);
    public static Font CONTENT_FONT_RED_UNDERLINE = new Font(BASEFONT, 10, Font.UNDERLINE, BaseColor.RED);
    public static Font CONTENT_FONT_GRAY = new Font(BASEFONT, 10, Font.NORMAL, BaseColor.GRAY);
    public static Font CONTENT_FONT_YELLOW = new Font(BASEFONT, 10, Font.UNDERLINE, BaseColor.YELLOW);
    public static Font CONTENT_FONT_ORANGE_UNDERLINE = new Font(BASEFONT, 10, Font.UNDERLINE, BaseColor.ORANGE);
    public static Font CONTENT_FONT_UNDERLINE = new Font(BASEFONT, 10, Font.UNDERLINE);


    public static final String PDF_HEAD_BLANK = "    ";
    public static final String WORD_HEAD_BLANK = "    ";
    @Autowired
    FunFileUtils funFileUtils;
    
    private static final float demo_2_real_pdf_percent = 0.24f;

    private static final int DOCUMENT_QCODE_WIDTH = 2032;

    /**
     * 创建文件
     *
     * @param file
     * @param document
     * @throws Exception
     */
    public PdfUtil(String file, Document document) throws Exception {
        // 创建PdfWriter, 类似还有HtmlWriter、RtfWriter、XmlWriter
        PdfWriter pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(new File(file)));
        //图片按照顺序生成
        pdfWriter.setStrictImageSequence(true);
    }
    
    public PdfUtil() {
    }

    /**
     * 获取中文字体
     *
     * @return
     */
    private static BaseFont getBaseFont() {
        BaseFont bfChinese = null;
        try {
            File file = new File("/usr/share/fonts/simsun.ttf");
            if (file.exists()) {
                bfChinese = BaseFont.createFont("/usr/share/fonts/simsun.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                log.info("font={}", "/usr/share/fonts/simsun.ttf");
            } else {
                bfChinese = BaseFont.createFont("C:/WINDOWS/Fonts/simsun.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                log.info("font={}", "C:/WINDOWS/Fonts/simsun.ttf");
            }
        } catch (Exception e) {
            try {
                bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
                log.info("font={}", "STSong-Light");
            } catch (DocumentException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return bfChinese;
    }

    /**
     * 创建空格
     *
     * @param size
     * @param document
     * @throws DocumentException
     */
    public void addBlank(int size, Document document) throws DocumentException {
        Paragraph paragraph = new Paragraph(" ", new Font((BASEFONT), size));
        document.add(paragraph);
    }

    /**
     * 创建标题头
     *
     * @param document
     * @param content  标题头内容
     * @throws DocumentException
     */
    public void addTitle(String content, Document document) throws DocumentException {
        Paragraph paragraph = new Paragraph(content, TITLE_FONT);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        document.add(paragraph);
    }

    /**
     * 创建内容头
     *
     * @param content
     * @param document
     * @throws DocumentException
     */
    public void addBaseTitle(String content, Document document) throws DocumentException {
        Paragraph paragraph = new Paragraph(content, CONTENT_TITILE_FONT);
        paragraph.setIndentationLeft(5);
        document.add(paragraph);
    }

    /**
     * 添加基本内容
     *
     * @param content
     * @param document
     * @throws Exception
     */
    public void addBaseContent(String content, Document document) throws Exception {
        content = content.replace("&lt;", "<");
        content = content.replace("&gt;", ">");
        Chunk chunk = instantiateChunk(content, CONTENT_FONT);
        Paragraph paragraph = new Paragraph(chunk);
        document.add(paragraph);
    }

    /**
     * 添加红色下划线字体`
     *
     * @param content
     * @param document
     * @throws Exception
     */
    public void addRedUnderLineContent(String content, Document document) throws Exception {
        Paragraph paragraph = new Paragraph(content, CONTENT_FONT_UNDERLINE);
        paragraph.setIndentationLeft(5);
        paragraph.setAlignment(Element.ALIGN_LEFT);
        document.add(paragraph);
    }

    /**
     * 添加小标题
     *
     * @param content
     * @param document
     * @throws Exception
     */
    public void addLittleTitle(String content, Document document) throws Exception {
        Paragraph paragraph = new Paragraph(content, TITLE_FONT);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        document.add(paragraph);
    }


    /**
     * 添加答题格
     *
     * @param document
     * @param sum
     * @throws DocumentException
     */
    public void addForm(int sum, Document document) throws DocumentException {
        int line = 25;
        int row = sum / 25 + 1;
        int num = 1;
        for (int i = 0; i < row; i++) {
            //创建PdfTable对象
            PdfPTable table = new PdfPTable(line);
            PdfPCell blankCell = new PdfPCell();
            blankCell.setMinimumHeight(19);
            blankCell.setBorderColor(new BaseColor(241, 158, 194));
            //设置各列的列宽
            table.setTotalWidth(480);
            table.setLockedWidth(true);
            for (int j = 0; j < line; j++) {

                table.addCell(blankCell);
            }
            document.add(table);
            if ((i + 1) * line % 100 == 0) {
                //创建数字
                Paragraph paragraph = new Paragraph("" + num * 100, new Font(BASEFONT, 8));
                paragraph.setIndentationLeft(470);
                num++;
                document.add(paragraph);
            } else {
                //创建空格
                Paragraph paragraph = new Paragraph(" ", new Font(BASEFONT, 6));
                document.add(paragraph);
            }
        }
    }

    /**
     * 添加水印
     *
     * @param text       加水印的文本内容
     * @param textWidth  文字横坐标
     * @param textHeight 文字纵坐标
     * @throws Exception
     */
    public void addWaterMark(String inputPath, String text, int textWidth, int textHeight) throws Exception {
        //创建临时文件
        String outputPath = "/app/logs/essay-server/pdf/2.pdf";
        // 待加水印的文件
        PdfReader reader = new PdfReader(inputPath);
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(outputPath));
        //定义页数
        int total = reader.getNumberOfPages() + 1;
        // 加完水印的文件
        waterMark(text, textWidth, textHeight, stamper, total);
        stamper.close();
    }

    /**
     * @param text
     * @param textWidth
     * @param textHeight
     * @param stamper
     * @param total
     */
    private void waterMark(String text, int textWidth, int textHeight, PdfStamper stamper, int total) {
        // 循环对每页插入水印
        for (int i = 1; i < total; i++) {
            // 水印的起始
            PdfContentByte content = stamper.getUnderContent(i);
            // 开始
            content.beginText();
            // 设置颜色
            content.setColorFill(BaseColor.GRAY);
            // 设置字体及字号
            content.setFontAndSize(BASEFONT, 10);
            // 设置起始位置
            content.setTextMatrix(textWidth, textHeight);
            // 开始写入水印
            //旋转度数默认45
            content.showTextAligned(Element.ALIGN_LEFT, text, textWidth,
                    textHeight, 0);
            // 结束
            content.endText();
        }
    }

    /**
     * 添加页眉横线
     *
     * @param stamper
     * @param total
     */
    private void pageTitle(PdfStamper stamper, int total) {
        for (int i = 1; i < total; i++) {
            // 水印的起始
            PdfContentByte content = stamper.getUnderContent(i);
            // 开始
            content.beginText();
            // 设置颜色
            content.setColorFill(BaseColor.GRAY);
            // 设置字体及字号
            content.setFontAndSize(BASEFONT, 10);
            // 设置起始位置
            content.setTextMatrix(350, 20);
            // 开始写入水印
            //旋转度数默认45
            content.showTextAligned(Element.ALIGN_LEFT, "——————————————————————————————————————————————————", 47,
                    780, 0);
            // 结束
            content.endText();
        }
    }

    /**
     * 添加页码
     *
     * @param stamper
     * @param total
     */
    private void pageMark(PdfStamper stamper, int total) {
        // 循环对每页插入页码 封皮和封底略过不需要加
		for (int i = 2; i < total - 1; i++) {
            // 水印的起始
            PdfContentByte content = stamper.getUnderContent(i);
            // 开始
            content.beginText();
            // 设置颜色
            content.setColorFill(BaseColor.GRAY);
            // 设置字体及字号
            content.setFontAndSize(BASEFONT, 10);
            // 设置起始位置
            content.setTextMatrix(350, 20);
            // 开始写入水印
            //旋转度数默认45
			//content.showTextAligned(Element.ALIGN_LEFT, "第" + i + "页共" + (total - 1) + "页", 287, 20, 0);
            content.showTextAligned(Element.ALIGN_LEFT,  i-1 + "/" + (total - 3), 287, 20, 0);
            // 结束
            content.endText();
        }
    }


    /**
     * 添加图片水印
     *
     * @param inputPath 写入文件
     * @throws IOException
     * @throws DocumentException
     */
    public String addWaterImage(String inputPath, String logo, String slogan, String vhuatu, String oldLogo) throws Exception {
        String outputPath = UUID.randomUUID().toString().replaceAll("-", "") + ".pdf";
        // String outputPath = "D:/" + UUID.randomUUID().toString() + ".pdf";
        log.info("---------------------创建pdf水印导入文件----------------------");
        PdfReader reader = new PdfReader(inputPath);
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(outputPath.replaceAll("null", "")));
        //定义页数
        int total = reader.getNumberOfPages() + 1;
        //加载水印
        log.info("--------------------开始添加水印----------------------");
        waterImage(64, 100, oldLogo, total, stamper);
//        //华图在线图标 logo
//        waterImageV2(48, 790, 0,1f,logo, total, stamper);
//        //过关才是硬道理 slogan
//        waterImageV2(460, 790, 0,1f,slogan, total, stamper);
//
//        //水印 vhuatu
//        for(int i = 0; i< 2;i++){
//            for(int j=0;j<3;j++){
//                waterImageV2(100+i*250, 600-j*200, 30,0.3f,vhuatu, total, stamper);
//            }
//
//        }
        //页眉横线
//        pageTitle(stamper, total);
        //页码
        pageMark(stamper, total);
        stamper.close();
        reader.close();
        log.info("--------------------水印添加结束将地址返回----------------------");
        return outputPath.replaceAll("null", "");
    }


    /**
     * @param x
     * @param y
     * @param imgPath
     * @param total
     * @param stamper
     * @throws Exception
     */
    public void waterImage(int x, int y, String imgPath, int total, PdfStamper stamper) throws Exception {
        //设置清晰度1f是完全清晰
        PdfGState gs1 = new PdfGState();
        gs1.setFillOpacity(1f);

        Image image = Image.getInstance(IOUtils.toByteArray(new FileInputStream(imgPath)));
        log.debug("pad x " + image.getDpiX() + "     pdf y " + image.getDpiY());
        image.setDpi(465, 712);
        log.debug("pad x " + image.getDpiX() + "     pdf y " + image.getDpiY());
        log.debug("image percentage" + image.getWidthPercentage());
        image.setWidthPercentage(34);
        log.debug("image percentage" + image.getWidthPercentage());
        for (int i = 1; i < total; i++) {
            PdfContentByte pdfContentByte = stamper.getOverContent(i);
            //加入图片格式
            pdfContentByte.setGState(gs1);
            //设置图片位置
            image.setAbsolutePosition(x, y);
            //生成图片
            pdfContentByte.addImage(image);
        }
    }

//    /**
//     * @param x
//     * @param y
//     * @param imgPath
//     * @param total
//     * @param stamper
//     * @throws Exception
//     */
//    public void waterImageV2(int x, int y, float rotation,float fillOpacity,String imgPath, int total, PdfStamper stamper) throws Exception {
//        //设置清晰度1f是完全清晰
//        PdfGState gs1 = new PdfGState();
//        gs1.setFillOpacity(fillOpacity);
//
//        Image image = Image.getInstance(IOUtils.toByteArray(new FileInputStream(imgPath)));
//        for (int i = 1; i < total; i++) {
//            PdfContentByte pdfContentByte = stamper.getUnderContent(i);
//            //加入图片格式
//            pdfContentByte.setGState(gs1);
//            //设置图片位置
//            image.setAbsolutePosition(x, y);
//            image.scalePercent(10);//依照比例缩放
//
////            image.scaleToFit(100,30);
//            image.setRotationDegrees(rotation);
//            image.setAlignment(Image.UNDERLYING);
//            //生成图片
//            pdfContentByte.addImage(image);
//
//
//        }
//    }

//    /**
//     * 添加图片和文字水印
//     *
//     * @param x
//     * @param y
//     * @param inputPath
//     * @param imgPath
//     * @param text
//     * @param textWidth
//     * @param textHeight
//     * @return
//     * @throws Exception
//     */
//    public String addwaterImgAndWord(int x, int y, String inputPath, String imgPath, String text, int textWidth, int textHeight) throws Exception {
//        String outputPath = "D:/" + UUID.randomUUID().toString() + ".pdf";
//        PdfReader reader = new PdfReader(inputPath);
//        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(outputPath));
//        //定义页数
//        int total = reader.getNumberOfPages() + 1;
//        //加载水印
//        waterImage(x, y, imgPath, total, stamper);
//        waterMark(text, textWidth, textHeight, stamper, total);
//        stamper.close();
//        reader.close();
//        return outputPath;
//    }

    public void correct(String str, Document document, int type, long answerId) throws Exception {
        Pattern pattern = Pattern.compile("。([0-9]\\.)");
        Matcher mather = pattern.matcher(str);
        int index = 0;
        String head = "";
        while (mather.find(index)) {
            int start = mather.start();
            correctSubStr(PDF_HEAD_BLANK + head + str.substring(index, start) + "。", document);
            index = mather.end();
            head = mather.group(1);
        }
        if (index < str.length()) {
            correctSubStr(PDF_HEAD_BLANK + str.substring(index), document);
        }

    }

    private void correctSubStr(String str, Document document) throws DocumentException {
        char[] charList = str.toCharArray();

        Paragraph paragraph = new Paragraph();
        StringBuilder sb = new StringBuilder();
        Font font = null;
        for (int i = 0; i < charList.length; i++) {
            if (charList[i] == '{' || charList[i] == '}' || charList[i] == '[' || charList[i] == ']' || charList[i] == '<' || charList[i] == '>') {
                font = addChunkContent(sb, font, charList[i], null, paragraph);
                continue;
            } else if (charList[i == 0 ? 0 : i - 1] == '<') {
                for (int j = i; j < charList.length; j++) {
                    if (charList[j == i ? i : j - 1] == '{') {
                        for (int k = j; k < charList.length; k++) {
                            if (charList[k] == '}') {
                                j = k;
                                break;
                            }
                            if ('{' != (charList[k]) && '}' != (charList[k])) {
                                font = addChunkContent(sb, font, charList[k], PdfUtil.CONTENT_FONT_ORANGE_UNDERLINE, paragraph);
                            } else {
                                k = k + 1;
                            }

                        }
                    }
                    if (charList[j] == '>') {
                        i = j;
                        break;
                    }
                    if ('{' != (charList[j]) && '}' != (charList[j])) {
                        font = addChunkContent(sb, font, charList[j], PdfUtil.CONTENT_FONT_ORANGE_UNDERLINE, paragraph);
                    }
                }
            } else if (charList[i == 0 ? 0 : i - 1] == '[') {
                for (int j = i; j < charList.length; j++) {
                    if (charList[j] == ']') {
                        i = j;
                        break;
                    }
                    if ('{' != (charList[j]) && '}' != (charList[j])) {
                        font = addChunkContent(sb, font, charList[j], PdfUtil.CONTENT_FONT_RED_UNDERLINE, paragraph);
                    }
                }
            } else if (charList[i == 0 ? 0 : i - 1] == '{') {
                for (int j = i; j < charList.length; j++) {
                    if (charList[j] == '}') {
                        i = j;
                        break;
                    }
                    if ('{' != (charList[j]) && '}' != (charList[j])) {
                        font = addChunkContent(sb, font, charList[j], PdfUtil.CONTENT_FONT_ORANGE_UNDERLINE, paragraph);
                    }
                }
            } else {
                if ('{' != (charList[i]) && '}' != (charList[i])) {
                    font = addChunkContent(sb, font, charList[i], PdfUtil.CONTENT_FONT, paragraph);
                }
            }
        }
        if (null != sb && sb.length() > 0) {
            Chunk chunk = instantiateChunk(sb.toString(), font);
            paragraph.add(chunk);
            sb.delete(0, sb.length());   //清空数据
        }

        document.add(paragraph);
    }

    private Font addChunkContent(StringBuilder sb, Font font, char c, Font newFont, Paragraph paragraph) {
        if (null == font) {
            sb.delete(0, sb.length());   //清空数据
        }
        if (null == font || font == newFont) {       //字符串开始写入
            sb.append(c);
            return newFont;
        }
        //字体发生变化，先存入之前的内容，再录入新的字符
        Chunk chunk = instantiateChunk(sb.toString(), font);
        paragraph.add(chunk);
        sb.delete(0, sb.length());   //清空数据
        if (null != newFont) {
            sb.append(c);
        }
        return null;
    }

    /**
     * 通过url生成指定的图片
     *
     * @param urlString
     * @param imageName
     */
    public void downloadPicture(String urlString, String imageName) {
        URL url = null;

        try {
            url = new URL(urlString);
            DataInputStream dataInputStream = new DataInputStream(url.openStream());
            // String imageName = ESSAY_PDF_PICTURE_DATA + ".jpg";
            FileOutputStream fileOutputStream = new FileOutputStream(new File(imageName));
            byte[] buffer = new byte[1024];
            int length;
            while ((length = dataInputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, length);
            }
            dataInputStream.close();
            fileOutputStream.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加图片水印
     *
     * @param inputPath 写入文件
     * @throws IOException
     * @throws DocumentException
     */
    public void addWaterImageForZip(String inputPath, String outPutPath, String logo, String slogan, String vhuatu, String oldLogo) throws Exception {
        log.info("---------------------创建pdf水印导入文件----------------------");
        PdfReader reader = new PdfReader(inputPath);
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(outPutPath));
        //定义页数
        int total = reader.getNumberOfPages() + 1;
        //加载水印
        log.info("--------------------开始添加水印----------------------");
        log.info("水印参数：" + "oldLogo{" + oldLogo + "}");
        waterImage(64, 100, oldLogo, total, stamper);
        //页码
        pageMark(stamper, total);
        stamper.close();
        reader.close();
        HtmlFileUtil.deleteFile(inputPath);
        log.info("--------------------水印添加结束将地址返回----------------------");
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


    /**
     * 分析批注标签内容获取详细批注内容
     *
     * @param correctedContent 答案批改后内容
     * @param document
     * @param isWriteContent   是否将批改后的文字答案写入pdf
     * @return
     */
    public List<TagPosition> correctManual(String correctedContent, Document document, boolean isWriteContent) throws DocumentException {
        StringBuilder sb = new StringBuilder(correctedContent);
        Pattern pattern = Pattern.compile("<[/]?label_([0-9]+)[^>]*>");
        Matcher matcher = pattern.matcher(sb);
        List<TagPosition> tagPositions = Lists.newArrayList();          //此处tagPosition存储一个标签的其实内容位置，及内容标注等信息，而不是存储单个标签
        int index = 0;
        Paragraph paragraph = new Paragraph();
        while (matcher.find(index)) {
            String seq = matcher.group(1);
            int start = matcher.start();
            boolean isTagHead = matcher.group().indexOf("/") == -1;
            if (isTagHead) {            //新的标签开始，则目前内容在标签或者文字内
                TagPosition build = TagPosition.builder()
                        .start(start)
                        .tagName(seq)
                        .build();
                Font font = getNoEndPosition2Font(tagPositions);
                //写入内容
                writeContent2Paragraph(paragraph, font, sb.substring(index, start), isWriteContent);
                //加入标签信息
                assemblingTagPosition(build, matcher.group());
                tagPositions.add(build);
            } else {
                Optional<TagPosition> first = tagPositions.stream().filter(i -> i.getTagName().equalsIgnoreCase(seq)).findFirst();
                if (first.isPresent()) {
                    TagPosition tagPosition = first.get();
                    Font font = getNoEndPosition2Font(tagPositions);
                    Font tagFont = getFontByTagPosition(tagPosition);       //标记编号的字体颜色
                    if (tagFont.equals(font)) {
                        writeContent2Paragraph(paragraph, font, sb.substring(index, start) + "(" + seq + ")", isWriteContent);
                    } else {
                        writeContent2Paragraph(paragraph, font, sb.substring(index, start), isWriteContent);
                        writeContent2Paragraph(paragraph, tagFont, "(" + seq + ")", isWriteContent);
                    }
                    tagPosition.setEnd(start);
                }
            }
            index = matcher.end();
        }
        if(index < sb.length()){
            writeContent2Paragraph(paragraph, CONTENT_FONT, sb.substring(index), isWriteContent);
        }
        if(!paragraph.isEmpty()){
            document.add(paragraph);
        }
        Collections.sort(tagPositions,Comparator.comparing(i->Integer.parseInt(i.getTagName())));
        return tagPositions;
    }

    private Font getNoEndPosition2Font(List<TagPosition> tagPositions) {
        TagPosition noEndPosition = getNoEndPosition(tagPositions);
        if (null == noEndPosition) {
            return CONTENT_FONT;
        }
        return getFontByTagPosition(noEndPosition);
    }

    private Font getFontByTagPosition(TagPosition noEndPosition) {
        boolean highLight = noEndPosition.isHighLight();
        boolean underLine = noEndPosition.isUnderLine();
        if (highLight) {
            if (underLine) {
                return CONTENT_FONT_RED_UNDERLINE;
            } else {
                return CONTENT_FONT_YELLOW;
            }
        } else {
            if (underLine) {
                return CONTENT_FONT_ORANGE_UNDERLINE;
            } else {
                return CONTENT_FONT;
            }
        }
    }

    /**
     * 寻找最近的一个未结束的标签
     *
     * @param tagPositions
     * @return
     */
    private TagPosition getNoEndPosition(List<TagPosition> tagPositions) {
        return tagPositions.stream().filter(i -> i.getEnd() == 0).max(Comparator.comparing(TagPosition::getStart)).orElse(null);
    }

    /**
     * 写入内容
     *
     * @param paragraph
     * @param font
     * @param content
     * @param isWriteContent
     */
    private void writeContent2Paragraph(Paragraph paragraph, Font font, String content, boolean isWriteContent) {
        if (StringUtils.isNotBlank(content) && isWriteContent) {
            Chunk chunk = instantiateChunk(content, font);
            paragraph.add(chunk);
        }
    }

    /**
     * 填充属性
     *
     * @param tagPosition
     * @param content(<label_1 seq="1" description="“其实，在我国国际地位日益提升的今天，工匠精神的传承是极其重要的，从非物质文化遗产的保护到政府群众工作的开展，“工匠精神”尤其发挥着它的作用。”基本体现了“国家的发展需要有工匠精神。”" score="-1.0" drawType="0">)
     */
    private static void assemblingTagPosition(TagPosition tagPosition, String content) {
        Pattern pattern = Pattern.compile("\" description=\"([^\"]+)\" score=\"([^\"]+)\" drawType=\"([0|1|2])\"");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            tagPosition.setDescription(matcher.group(1));
            tagPosition.setScore(Double.parseDouble(matcher.group(2)));
            int drawType = Integer.parseInt(matcher.group(3));
            tagPosition.setHighLight(drawType != 0);
            tagPosition.setUnderLine(drawType != 1);
        }
    }
    

    public static void main(String[] args) {
        String content = "<label_1 seq=\"1\" description=\"“其实，在我国国际地位日益提升的今天，工匠精神的传承是极其重要的，从非物质文化遗产的保护到政府群众工作的开展，“工匠精神”尤其发挥着它的作用。”基本体现了“国家的发展需要有工匠精神。”\" score=\"-1.0\" drawType=\"0\">";
        TagPosition build = TagPosition.builder().build();
        assemblingTagPosition(build, content);
        System.out.println("new Gson() = " + new Gson().toJson(build));

    }
    
    public void addCoverTitle(String content, Document document) throws DocumentException {
        Paragraph paragraph = new Paragraph(content, COVER_TITLE_FONT);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        document.add(paragraph);
    }
    

    /**
     * 添加封面
     * @param document
     * @param mock
     */
	public void addCover(Document document, EssayMockExam mock, String paperName) throws Exception{
		addBlank(47, document);
		if (mock != null && mock.getStatus() == YesNoEnum.YES.getValue()) {
			paperName = mock.getName();
		}
		addCoverTitle(paperName, document);
		// 添加水印图片
        Image image = Image.getInstance(FunFileUtils.ESSAY_PDF_COVER_DOWNLOAD_URL);
        image.setScaleToFitLineWhenOverflow(true);
        image.setScaleToFitHeight(true);
        image.setAlignment(Image.TEXTWRAP);
        image.scaleAbsolute(DOCUMENT_QCODE_WIDTH * demo_2_real_pdf_percent, (DOCUMENT_QCODE_WIDTH * 368 / 976) * demo_2_real_pdf_percent);
        float absoluteX = 220 * demo_2_real_pdf_percent;
        float absoluteY = 318 * demo_2_real_pdf_percent;

        image.setAbsolutePosition(absoluteX, absoluteY);
        document.add(image);

		document.newPage();
	}

	/**
	 * 模考试卷添加封底
	 * @param document
	 * @param mock
	 * @throws IOException 
	 * @throws MalformedURLException 
	 * @throws DocumentException 
	 */
	public void addLastPage(Document document, EssayMockExam mock)
			throws MalformedURLException, IOException, DocumentException {
		document.newPage();
		// 添加水印图片
		Image image = Image.getInstance(FunFileUtils.ESSAY_PDF_COVER_ADVERT_URL);
		image.setScaleToFitLineWhenOverflow(true);
		image.setScaleToFitHeight(true);
		image.setAlignment(Image.TEXTWRAP);
		float height = (DOCUMENT_QCODE_WIDTH * 334 / 2010) * demo_2_real_pdf_percent;
		image.scaleAbsolute(DOCUMENT_QCODE_WIDTH * demo_2_real_pdf_percent, height);
		float absoluteX = 220 * demo_2_real_pdf_percent;
		float absoluteY = (document.topMargin() + document.top()) / 2 - height / 2;

		image.setAbsolutePosition(absoluteX, absoluteY);
		document.add(image);

	}
	
	/**
	 * 添加封底广告
	 * @param writer
	 * @param document
	 */
	public void addLastPageAdvert(PdfWriter writer, Document document) {
		document.newPage();
		Font boldFont = new Font(BASEFONT, 14, Font.BOLD);
		Font normalFont = new Font(BASEFONT, 14, Font.NORMAL);

		Phrase phrase1 = new Phrase();
		Chunk chunk1 = new Chunk("华图在线搜索：", normalFont);
		Chunk chunk2 = new Chunk("系统提分班", boldFont);
		phrase1.add(chunk1);
		phrase1.add(chunk2);
		Phrase phrase2 = new Phrase();
		Chunk chunk3 = new Chunk("名师授课 超高性价比 一站式满足公务员备考所有需求", normalFont);
		phrase2.add(chunk3);
		float absoluteY = (document.topMargin() + document.top()) / 2 - (boldFont.getSize() * 3) / 2;
		ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, phrase1, PageSize.A4.getWidth() / 2,
				absoluteY + 2 * boldFont.getSize(), 0);
		ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, phrase2, PageSize.A4.getWidth() / 2,
				absoluteY, 0);

	}
	
}
