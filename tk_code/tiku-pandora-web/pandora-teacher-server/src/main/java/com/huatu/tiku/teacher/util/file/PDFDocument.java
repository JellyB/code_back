package com.huatu.tiku.teacher.util.file;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.teacher.service.impl.download.v1.PdfWriteServiceImplV1;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;


/**
 * PDF 生成工具类
 * Created by lijun on 2018/11/15
 */
public class PDFDocument {


    private static List<String> imgUrl = Lists.newArrayList(
            "http://tiku.huatu.com/cdn/images/vhuatu/tiku/v/vn1WdQGE4QUrRiYUw5_ldlyHF0A.png",
            "http://tiku.huatu.com/cdn/images/vhuatu/tiku/s/sMQFEWkDJF8kzHuTvOZtHX9TvS_.png",
            "http://tiku.huatu.com/cdn/images/vhuatu/tiku/u/uZQYVq93SFGXYOCo8QKrp6dtxAp.png",
            "http://tiku.huatu.com/cdn/images/vhuatu/tiku/r/rKtfH_KjLZc5kCeBZEDVEM132Ym.png"
    );

    public static void main(String[] args) throws IOException, DocumentException {
        buildDocument();
    }

    public static void buildDocument() throws IOException, DocumentException {
        //页面大小，左、右、上、下边距
        Document document = new Document(PageSize.A4, 60, 60, 60, 60);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter.getInstance(document, outputStream);
            //以下信息，会在右键文件详情时候展示
            document.addCreationDate();
            document.addTitle("标题");//标题
            document.addAuthor("作者信息");//作者信息
            document.addCreator("内容创建者");//创建者信息
            document.addSubject("描述");//描述
            document.open();


            //添加文本
            document.add(new Paragraph("A hello word"));
            Image image = transImagePathToImage(imgUrl.get(0));
            document.add(image);


//            handleImageList(imgUrl, document);

            document.close();
            byte[] byteArray = outputStream.toByteArray();
            FileOutputStream fileOutputStream = new FileOutputStream("/Users/junli/Downloads/HelloWorld.pdf");
            fileOutputStream.write(byteArray);
        }
    }

    /**
     * 生成图片显示策略 使用 table定位
     * 传入的图片等宽，且数量为 2^n
     *
     * @param imageUrlList 图片地址信息
     * @return PdfPTable
     */
    public static void handleImageList(List<String> imageUrlList, Document document) throws DocumentException, IOException {
        if (CollectionUtils.isEmpty(imageUrlList)) {
            return;
        }
        //生成的列数量
        Image image = transImagePathToImage(imageUrlList.get(0));
        image.setAlignment(Image.LEFT | Image.TEXTWRAP);
        float plainWidth = image.getPlainWidth() + 15;
        int columnNum = (int) (getDocumentTrueWidth(document) / plainWidth);
        columnNum = columnNum % 2 == 0 ? columnNum : (columnNum - 1) == 0 ? 1 : (columnNum - 1);
        columnNum = Math.min(imageUrlList.size(), columnNum);
        //生成列表
        final PdfPTable pdfPTable = new PdfPTable(columnNum);
        pdfPTable.setHorizontalAlignment(Element.ALIGN_BOTTOM);//设置内容水平居中显示
        pdfPTable.setWidthPercentage(100);
        pdfPTable.setSkipFirstHeader(true);
        pdfPTable.setSkipLastFooter(false);
        pdfPTable.setSpacingBefore(2);
        //pdfPTable.setWidths(); 设置宽度比
        pdfPTable.getDefaultCell().setBorderWidth(0);//边框 0
        imageUrlList.stream()
                .map(PDFDocument::transImagePathToImage)
                .filter(imageEntity -> null != imageEntity)
                .forEach(imageEntity -> {
                    PdfPCell cell = createDefaultImagePdfPCell(imageEntity, Element.ALIGN_LEFT, Element.ALIGN_BOTTOM);
//                    cell.setFixedHeight(imageEntity.getPlainHeight());
                    pdfPTable.addCell(cell);
                });
        document.add(pdfPTable);
    }

    /**
     * 多个大图片可以同行处理的问题(单纯大图片的排列，不涉及选项)
     *
     * @param elementInfo pdf元素结集合
     * @return PdfPTable
     */
    public static void handleImageList(PdfWriteServiceImplV1.ElementInfo elementInfo, Document document) throws DocumentException, IOException {
        if (null == elementInfo || CollectionUtils.isEmpty(elementInfo.getElements())) {
            return;
        }
        //生成的列数量
        ArrayList<PdfElement> elements = elementInfo.getElements();     //需要写入的图片数量
        float plainWidth = elements.stream().map(PdfElement::getWidth).max(Float::compareTo).get() + 15;
        int columnNum = (int) (getDocumentTrueWidth(document) / plainWidth);
        if (columnNum == 0) {
            columnNum = 1;
        } else {
            columnNum = columnNum % 2 == 0 ? columnNum : (columnNum - 1) == 0 ? 1 : (columnNum - 1);
            columnNum = Math.min(elements.size(), columnNum);
        }
        if (columnNum < elements.size() && elements.size() % columnNum != 0) {  //列小于图片数量，且图片数量不能被列整除，则列数置为1
            columnNum = 1;
        }
        if (columnNum == 1) {
            for (PdfElement pdfElement : elementInfo.getElements()) {
                PDFDocument.handleImageList(pdfElement, document, Image.MIDDLE);
            }
            return;
        }
        //生成列表（处理小图片和文字混排的情况）
        final PdfPTable pdfPTable = new PdfPTable(columnNum);
        pdfPTable.setHorizontalAlignment(Element.ALIGN_BOTTOM);//设置内容水平居中显示
        pdfPTable.setWidthPercentage(100);
        pdfPTable.setSpacingBefore(2);
        pdfPTable.setSkipLastFooter(false);
        pdfPTable.setSkipFirstHeader(false);
        //pdfPTable.setWidths(); 设置宽度比
        pdfPTable.getDefaultCell().setBorderWidth(0);//边框 0
        for (PdfElement element : elements) {
            PdfPCell cell = new PdfPCell();
            cell.setBorderWidth(0);
            pdfPTable.addCell(cell);
        }
        elements.stream()
                .forEach(pdfElement -> {
                    PdfPCell cell = createDefaultTextPdfPCell(pdfElement, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE);
                    pdfPTable.addCell(cell);
                });
        document.add(pdfPTable);
    }

    /**
     * 全是文本内容（无图片的选项写入）(选项内容处理)
     *
     * @param elementInfo
     * @param document
     */
    private static void handlerTextList(PdfWriteServiceImplV1.ElementInfo elementInfo, Document document) {
        if (null == elementInfo || CollectionUtils.isEmpty(elementInfo.getElements())) {
            return;
        }
        //生成的列数量
        ArrayList<PdfElement> elements = elementInfo.getElements();     //选项的数量
        float plainWidth = elements.stream().map(PdfElement::getWidth).max(Float::compareTo).get() + 15;    //最大选项的宽度
        int columnNum = (int) (getDocumentTrueWidth(document) / plainWidth);
        if (columnNum == 0) {
            columnNum = 1;
        } else {
            columnNum = columnNum % 2 == 0 ? columnNum : (columnNum - 1) == 0 ? 1 : (columnNum - 1);
            columnNum = Math.min(elements.size(), columnNum);
        }
        if (columnNum < elements.size() && elements.size() % columnNum != 0) {  //列小于选项数量，且选项数量不能被列整除，则列数置为1
            columnNum = 1;
        }
        Element tempElement = elementInfo.getElements().get(0).getElement();
        if (columnNum == 1 && tempElement instanceof Phrase) { //选项一行只能放一个的，直接以段落处理
            elements.stream()
                    .map(PdfElement::getElement)
                    .filter(element -> null != element)
                    .filter(element -> element instanceof Phrase)
                    .map(i -> (Phrase) i)
                    .forEach(phrase -> {
                        Paragraph paragraph = new Paragraph(phrase);
                        try {
                            document.add(paragraph);
                        } catch (DocumentException e) {
                            e.printStackTrace();
                        }
                    });
            return;
        }
        //生成列表（一行有多列的）
        final PdfPTable pdfPTable = new PdfPTable(columnNum);
        pdfPTable.setHorizontalAlignment(Element.ALIGN_BOTTOM);//设置内容水平居中显示
        pdfPTable.setWidthPercentage(100);
        pdfPTable.setSpacingBefore(2);
        pdfPTable.setSkipLastFooter(false);
        pdfPTable.setSkipFirstHeader(false);
        //pdfPTable.setWidths(); 设置宽度比
        pdfPTable.getDefaultCell().setBorderWidth(0);//边框 0
        elements.stream()
                .forEach(element -> {
                    PdfPCell cell = createDefaultTextPdfPCell(element, Element.ALIGN_LEFT, Element.ALIGN_BOTTOM);
                    pdfPTable.addCell(cell);
                });
        try {
            document.add(pdfPTable);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取PDF 文件的实际可用宽度
     *
     * @param document PDF 文件
     * @return 宽度
     */
    private static float getDocumentTrueWidth(Document document) {
        return document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin();
    }

    /**
     * 图片路径 转换成 图片对象
     *
     * @param path 路径
     * @return 图片对象
     */
    private static Image transImagePathToImage(String path) {
        try {
            return Image.getInstance(new URL(path));
        } catch (BadElementException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 生成默认的 PdfPCell
     *
     * @param horizontal 垂直居中方式
     * @param vertical   水平居中方式
     * @return 默认 PdfPCell
     */
    private static PdfPCell createDefaultImagePdfPCell(Image image, int horizontal, int vertical) {
        PdfPCell pdfPCell = new PdfPCell(image, false);
        pdfPCell.setHorizontalAlignment(horizontal);
        pdfPCell.setVerticalAlignment(vertical);  // 设置垂直居中
        pdfPCell.setBorderWidth(0);
        return pdfPCell;
    }

    /**
     * 生成默认的 PdfPCell
     *
     * @param pdfElement
     * @param horizontal 垂直居中方式
     * @param vertical   水平居中方式
     * @return 默认 PdfPCell
     */
    private static PdfPCell createDefaultTextPdfPCell(PdfElement pdfElement, int horizontal, int vertical) {
        Element element = pdfElement.getElement();
        if (null == element) {
            PdfPCell cell = new PdfPCell();
            cell.setBorderWidth(0);
            return cell;
        }
        PdfPCell pdfPCell = null;
        if (element instanceof Phrase) {
            Paragraph paragraph = new Paragraph((Phrase) element);
            pdfPCell = new PdfPCell(paragraph);
        } else if (element instanceof Chunk) {
            Image image = ((Chunk) element).getImage();
            if (null == image) {
                ((Chunk) element).setUnderline(0.1f, -1f);
                Paragraph paragraph = new Paragraph((Chunk) element);
                pdfPCell = new PdfPCell(paragraph);
            } else {
                image.scaleToFit(pdfElement.getWidth(), pdfElement.getHeight());
                pdfPCell = new PdfPCell(image);
            }
        }
        pdfPCell.setHorizontalAlignment(horizontal);
        pdfPCell.setVerticalAlignment(vertical);  // 设置垂直居中
        pdfPCell.setBorderWidth(0);
        return pdfPCell;
    }

    /**
     * 设定选项的逻辑
     * 数据源：多个选项的数据整合分组
     *
     * @param elementList 多个选项的元素集合组成的集合
     * @param choices     选项内容
     * @param document
     * @throws IOException
     * @throws DocumentException
     */
    public static void handleChoice(LinkedList<PdfWriteServiceImplV1.ElementInfo> elementList, List<String> choices, Document document) throws IOException, DocumentException {
        ArrayList<PdfWriteServiceImplV1.ElementInfo> elementInfos = Lists.newArrayList(elementList);    //elementList会被清空，需要备份一份做后期计算
        if (CollectionUtils.isEmpty(elementList) || CollectionUtils.isEmpty(choices)) {
            return;
        }
        int size = choices.size();  //选项个数
        int elementNum = elementList.size();    //选项内容分组情况
        //最大分组的内的元素个数（元素个数决定了元素的差异性）
        int maxElementSize = elementList.stream().map(PdfWriteServiceImplV1.ElementInfo::getElements).map(List::size).max(Integer::compare).get();
        if (elementNum == size) {        //选项内容中无大图，不需要换行
            if (maxElementSize == 1) {        // 即内容和选项都是文字
                PdfWriteServiceImplV1.ElementInfo elementInfo = new PdfWriteServiceImplV1.ElementInfo();
                for (PdfWriteServiceImplV1.ElementInfo info : elementList) {
                    elementInfo.addAll(info.getElements(), info.getMaxHeight());
                }
                handlerTextList(elementInfo, document);
            } else {                  //选项中含有图片但是都是小图
                float maxWidth = 0;
                for (PdfWriteServiceImplV1.ElementInfo elementInfo : elementInfos) {
                    ArrayList<PdfElement> elements = elementInfo.getElements();
                    float width = 0;
                    for (PdfElement element : elements) {
                        width += element.getWidth();
                    }
                    maxWidth = Math.max(maxWidth, width);
                }
                float maxHeight = elementInfos.stream().map(PdfWriteServiceImplV1.ElementInfo::getMaxHeight).max(Float::compareTo).get();
                handleMultiChoice(elementList, document, maxWidth, maxHeight);
            }
        } else {                 //存在大图
            boolean choiceWithImgFlag = elementList.stream().map(PdfWriteServiceImplV1.ElementInfo::getElements)
                    .filter(i -> i.size() == 1)                     //单独一个element为一个单位（证明有大图）
                    .filter(i -> {                                //选项A.被单独隔离出来
                        Element element = i.get(0).getElement();
                        if (element instanceof Phrase) {
                            return ((Phrase) element).getContent().length() == 2;
                        }
                        return false;
                    }).findAny().isPresent();           //是否选项被单独隔离出来
            if (2 * size == elementNum                  //1.选项内容分两部分
                    && maxElementSize == 1              //2.每部分只有一个element，即图片和文字不在一起
                    && !choiceWithImgFlag) {                     //3.文字部分不止（A.）
                //选项格式：  A.+文字+大图
                List<List<PdfElement>> pdfList = elementList.stream().map(i -> {
                    List<PdfElement> list = Lists.newArrayList();
                    list.addAll(clearBrTag(i.getElements()));
                    return list;
                }).collect(Collectors.toList());
                handleChoice(pdfList, document);
            } else {                                                    //选项中存在大图（并且大图的分布不规律，比如大图夹杂在文字中间），策略:一个选项一行
                List<List<PdfElement>> pdfList = Lists.newArrayList();
                for (int i = 0; i < size; i++) {
                    ArrayList<PdfElement> elements = Lists.newArrayList();
                    //确认选项起点
                    char ch = (char) ('A' + i + 1);
                    String choiceFlag = ch + ".";
                    while (true) {
                        if (CollectionUtils.isEmpty(elementList)) {
                            break;
                        }
                        PdfWriteServiceImplV1.ElementInfo elementInfo = elementList.removeFirst();
                        PdfElement pdfElement = elementInfo.getElements().get(0);
                        Element element = pdfElement.getElement();
                        if (element instanceof Phrase) {
                            String content = ((Phrase) element).getContent();
                            if (StringUtils.isNotBlank(content) && content.indexOf(choiceFlag) > -1) {          //检索到下个选项的内容归还数据
                                elementList.addFirst(elementInfo);
                                break;
                            }
                        }
                        elements.addAll(clearBrTag(elementInfo.getElements()));
                    }
                    pdfList.add(elements);
                }
                //选项中含有大图的情况
                handleChoice(pdfList, document);
            }
        }

    }


    private static Collection<? extends PdfElement> clearBrTag(ArrayList<PdfElement> elements) {
        ArrayList<PdfElement> result = Lists.newArrayList();
        for (PdfElement element : elements) {
            Element element1 = element.getElement();
            if (element1 instanceof Phrase) {
                String content = ((Phrase) element1).getContent();
                if (StringUtils.isNotBlank(content)) {
                    PdfElement pdfElement = new PdfElement();
                    String replace = content.replace("\n", "");
                    pdfElement.setElement(new Phrase(replace, ((Phrase) element1).getFont()));
                    result.add(pdfElement);
                } else {
                    result.add(element);
                }
            } else {
                result.add(element);
            }
        }
        return result;
    }

    /**
     * 处理多个选项同行(且文字和图片混排的问题)---混排的表格内容尽量不要给表格的设定高度
     *
     * @param elementList
     * @param document
     * @param maxWidth
     * @param maxHeight
     * @throws DocumentException
     */
    private static void handleMultiChoice(LinkedList<PdfWriteServiceImplV1.ElementInfo> elementList, Document document, float maxWidth, float maxHeight) throws DocumentException {
        if (CollectionUtils.isEmpty(elementList)) {
            return;
        }
        //生成的列数量
        float plainWidth = maxWidth + 25;
        int columnNum = (int) (getDocumentTrueWidth(document) / plainWidth);
        if (columnNum == 0) {
            columnNum = 1;
        } else {
            columnNum = columnNum % 2 == 0 ? columnNum : (columnNum - 1) == 0 ? 1 : (columnNum - 1);
            columnNum = Math.min(elementList.size(), columnNum);
        }
        //生成列表
        final PdfPTable pdfPTable = new PdfPTable(columnNum);
        pdfPTable.setHorizontalAlignment(Element.ALIGN_BOTTOM);//设置内容水平居中显示
        pdfPTable.setWidthPercentage(100);
        pdfPTable.setSpacingBefore(5);
        pdfPTable.setSkipLastFooter(false);
        pdfPTable.setSkipFirstHeader(false);
        //pdfPTable.setWidths(); 设置宽度比
        pdfPTable.getDefaultCell().setBorderWidth(0);//边框 0
        for (int k = 0; k < columnNum; k++) {
            PdfPCell pdfPCell = new PdfPCell();
            pdfPCell.setBorderWidth(0);
            pdfPTable.addCell(pdfPCell);
        }
        for (PdfWriteServiceImplV1.ElementInfo elementInfo : elementList) {
            ArrayList<PdfElement> elements = elementInfo.getElements();
            Paragraph paragraph = new Paragraph();
            elements.stream()
                    .filter(pdfElement -> null != pdfElement)
                    .forEach(pdfElement -> {
                        Element element = pdfElement.getElement();
                        if (element instanceof Phrase) {
                            paragraph.add(element);
                        } else if (element instanceof Chunk) {
                            Image image = ((Chunk) element).getImage();
                            if (image == null) {
                                ((Chunk) element).setUnderline(0.1f, -1f);
                                paragraph.add(element);
                            }
                            image.scaleToFit(pdfElement.getWidth(), pdfElement.getHeight());
                            paragraph.add(new Chunk(image, 0, (11 - pdfElement.getHeight()) / 2));
                        } else if (element instanceof Image) {
                            paragraph.add(element);
                        }
                    });
            PdfPCell pdfPCell = new PdfPCell(paragraph);
            pdfPCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            pdfPCell.setVerticalAlignment(Element.ALIGN_MIDDLE);  // 设置垂直居中
            if (maxHeight > 20) {     //高度大于20的图片选项，表格设定固定高度
                pdfPCell.setFixedHeight(maxHeight);
            }
            pdfPCell.setBorderWidth(0);
            pdfPTable.addCell(pdfPCell);
        }
        document.add(pdfPTable);
    }

    /**
     * 生成图片显示策略 使用 table定位
     * 选项中带大图片专用
     *
     * @param pdfList pdf元素结集合
     * @return PdfPTable
     */
    public static void handleChoice(List<List<PdfElement>> pdfList, Document document) throws DocumentException, IOException {
        //生成的列数量
        int columnNum = 1;
        //生成列表
        final PdfPTable pdfPTable = new PdfPTable(columnNum);
        pdfPTable.setSkipLastFooter(false);
        pdfPTable.setSkipFirstHeader(false);
        pdfPTable.setSpacingBefore(2);
        pdfPTable.setHorizontalAlignment(Element.ALIGN_BOTTOM);//设置内容水平居中显示
        pdfPTable.setWidthPercentage(100);
        pdfPTable.getDefaultCell().setBorderWidth(0);//边框 0
        //pdfPTable.setWidths(); 设置宽度比
        for (List<PdfElement> elements : pdfList) {
            PdfPCell tempCell = new PdfPCell();
            tempCell.setBorderWidth(0);
            pdfPTable.addCell(tempCell);
            Paragraph paragraph = new Paragraph();
            paragraph.setAlignment(Paragraph.ALIGN_LEFT | Paragraph.ALIGN_BOTTOM);
            for (PdfElement pdfElement : elements) {
                Element element = pdfElement.getElement();
                if (element instanceof Chunk && null != ((Chunk) element).getImage()) {
                    paragraph.add(new Chunk(((Chunk) element).getImage(), 0f, 0f));
                } else {
                    paragraph.add(element);
                }
            }
            PdfPCell pdfPCell = new PdfPCell(paragraph);
            pdfPCell.setFixedHeight(elements.stream().map(PdfElement::getHeight).max(Float::compareTo).get());
            pdfPCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            pdfPCell.setVerticalAlignment(Element.ALIGN_BOTTOM);  // 设置垂直居中
            pdfPCell.setBorderWidth(0);
            pdfPTable.addCell(pdfPCell);
        }
        document.add(pdfPTable);
    }

    /**
     * 居中单个图片处理
     *
     * @param pdfElement
     * @param document
     * @param location
     * @throws DocumentException
     */
    public static void handleImageList(PdfElement pdfElement, Document document, int location) throws DocumentException {
        //生成的列数量
        int columnNum = 1;
        //生成列表
        final PdfPTable pdfPTable = new PdfPTable(columnNum);
        pdfPTable.setHorizontalAlignment(location);//设置内容水平居中显示
        pdfPTable.setWidthPercentage(100);
        pdfPTable.getDefaultCell().setBorderWidth(0);//边框 0
        pdfPTable.setSkipFirstHeader(true);
        pdfPTable.setSkipLastFooter(true);
        pdfPTable.setSpacingBefore(2);
        //这个表格的作用是出现页尾放不下第二个图片表格时，将第二个图片表格挤到下一行去展示完整大小的图片，而不是在上一页压缩展示图片
        PdfPCell tempCell = new PdfPCell();
        tempCell.setBorderWidth(0);
        pdfPTable.addCell(tempCell);
        //pdfPTable.setWidths(); 设置宽度比
        Float height = pdfElement.getHeight();
        Element element = pdfElement.getElement();
        if (element instanceof Chunk) {
            Image image = ((Chunk) element).getImage();
            if (null == image) {
                return;
            }
            image.setAlignment(location);
            image.scaleToFit(pdfElement.getWidth(), pdfElement.getHeight());
            PdfPCell pdfPCell = new PdfPCell(image, false);
            pdfPCell.setHorizontalAlignment(location);
            pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);  // 设置垂直居中
            pdfPCell.setFixedHeight(height);
            pdfPCell.setBorderWidth(0);
            pdfPTable.addCell(pdfPCell);
        }
        document.add(pdfPTable);
    }

    /**
     * 使用表格规范小图片居中对齐的问题
     *
     * @param elements
     * @param document
     * @param maxHeight 由于段落问题，需要判断某一行的数据是否需要用表格矫正
     */
    public static void handleImageParagraph(ArrayList<PdfElement> elements, Document document, float maxHeight) {
        float documentTrueWidth = getDocumentTrueWidth(document);
        ArrayList<PdfElement> tempList = Lists.newArrayList();

        for (PdfElement pdfElement : elements) {
            boolean fillFlag = checkDocumentWidth(tempList, pdfElement, documentTrueWidth); //是否还可以添加元素
            if (fillFlag) {
                tempList.add(pdfElement);
                continue;
            } else {
                Element element = pdfElement.getElement();
                if (element instanceof Chunk) {   //图片
                    Image image = ((Chunk) element).getImage();
                    if (null == image) {      //处理下划线
                        PdfElement tempPdfElement = splitPdfElement(tempList, pdfElement, documentTrueWidth);
                        addTable(tempList, documentTrueWidth, document, true, maxHeight);
                        tempList.clear();
                        tempList.add(tempPdfElement);
                    } else {
                        addTable(tempList, documentTrueWidth, document, false, maxHeight);
                        tempList.clear();
                        tempList.add(pdfElement);
                    }
                } else if (element instanceof Phrase) {
                    PdfElement tempPdfElement = splitPdfElement(tempList, pdfElement, documentTrueWidth);
                    addTable(tempList, documentTrueWidth, document, true, maxHeight);
                    tempList.clear();
                    tempList.add(tempPdfElement);
                }
            }
        }
        if (CollectionUtils.isNotEmpty(tempList)) {
            addTable(tempList, documentTrueWidth, document, false, maxHeight);
        }


    }

    /**
     * 添加表格（字体和大图混排需要居中处理）
     *
     * @param tempList
     * @param documentTrueWidth
     * @param document
     * @param widthFlag         是否宽度与document宽度一致
     * @param maxHeight
     */
    private static void addTable(ArrayList<PdfElement> tempList, float documentTrueWidth, Document document, boolean widthFlag, float maxHeight) {
        if (CollectionUtils.isEmpty(tempList)) {
            return;
        }
        Float height = tempList.stream().map(PdfElement::getHeight).max(Float::compare).orElse(0f);
        if (height < maxHeight) {     //该行不需要表格矫正
            Paragraph paragraph = new Paragraph("");
            for (PdfElement pdfElement : tempList) {
                paragraph.add(pdfElement.getElement());
            }
            try {
                document.add(paragraph);
            } catch (DocumentException e) {
                e.printStackTrace();
            }
            return;
        } else if (tempList.size() == 1) {       //单个图片居中处理
            try {
                handleImageList(tempList.get(0), document, Image.MIDDLE);
            } catch (DocumentException e) {
                e.printStackTrace();
            }
            return;
        }
        PdfPTable table = createTable(tempList, widthFlag, documentTrueWidth);
        try {
            document.add(table);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * 拆分pdfElement元素，其中左边元素作为tempList的最后元素，右边元素返回作为下一行的首个元素
     *
     * @param tempList
     * @param pdfElement
     * @param documentTrueWidth
     * @return
     */
    private static PdfElement splitPdfElement(ArrayList<PdfElement> tempList, PdfElement pdfElement, float documentTrueWidth) {
        float blankSize = documentTrueWidth;
        if (CollectionUtils.isNotEmpty(tempList)) {
            for (PdfElement element : tempList) {
                blankSize = blankSize - element.getWidth();
            }
        }
        float width = pdfElement.getWidth();
        if (width < blankSize) {        //此类问题理论上不存在
            System.out.println("width = " + width + "||blankSize = " + blankSize);
            return pdfElement;
        }
        Element element = pdfElement.getElement();
        if (element instanceof Phrase) {
            String content = ((Phrase) element).getContent();
            Font font = ((Phrase) element).getFont();
            int index = getStringIndeByFont(content, blankSize, width, font);
            PdfElement leftPdfElement = new PdfElement(new Phrase(content.substring(0, index), font));
            tempList.add(leftPdfElement);
            PdfElement rightPdfElement = new PdfElement(new Phrase(content.substring(index), font));
            return rightPdfElement;
        } else if (element instanceof Chunk) {
            String content = ((Chunk) element).getContent();
            Font font = ((Chunk) element).getFont();
            int index = getStringIndeByFont(content, blankSize, width, font);
            Chunk chunk = PdfUtil.instantiateChunk(content.substring(0, index), font);
            chunk.setUnderline(0.1f, -1f);
            PdfElement leftPdfElement = new PdfElement(chunk);
            tempList.add(leftPdfElement);
            Chunk rightChunk = PdfUtil.instantiateChunk(content.substring(index), font);
            rightChunk.setUnderline(0.1f, -1f);
            PdfElement rightPdfElement = new PdfElement(rightChunk);
            return rightPdfElement;
        }
        System.out.println("拆分失败！，element = " + element);
        return pdfElement;
    }

    /**
     * 根据字体确定多长的字符串可以被放入容纳
     *
     * @param content   字符串操作目标
     * @param blankSize 字体预留长度
     * @param width     字符串子题长度
     * @param font      字体
     * @return
     */
    private static int getStringIndeByFont(String content, float blankSize, float width, Font font) {
        int tempIndex = (int) (content.length() * blankSize / width);
        HashMap<Integer, Float> map = Maps.newHashMap();
        int leftIndex = 0;
        int rightIndex = 0;
        while (true) {
            if (tempIndex > content.length() || tempIndex <= 0) {
                break;
            }
            float fontLength = getFontLength(content.substring(0, tempIndex), font);
            map.put(tempIndex, blankSize - fontLength);
            if (blankSize > fontLength) {
                leftIndex = tempIndex;
                tempIndex++;
            } else {
                rightIndex = tempIndex;
                tempIndex--;
            }
            if (rightIndex != 0 && leftIndex != 0) {
                break;
            }
        }
        return leftIndex;
    }

    public static float getFontLength(String substring, Font font) {
        BaseFont baseFont = font.getCalculatedBaseFont(true);
        float widthPoint = baseFont.getWidthPoint(substring, font.getSize());
        return widthPoint;
    }

    /**
     * 是否能塞入element
     *
     * @param tempList
     * @param element
     * @param documentTrueWidth
     * @return
     */
    private static boolean checkDocumentWidth(ArrayList<PdfElement> tempList, PdfElement element, float documentTrueWidth) {
        float widthSum = 0f;
        if (CollectionUtils.isNotEmpty(tempList)) {
            for (PdfElement pdfElement : tempList) {
                widthSum += pdfElement.getWidth();
            }
        }
        widthSum += element.getWidth();
        return documentTrueWidth > widthSum;
    }

    /**
     * 创建单行表格，需要设定列宽
     *
     * @param elements
     * @param widthFlag
     * @param documentTrueWidth
     * @return
     */
    private static PdfPTable createTable(ArrayList<PdfElement> elements, boolean widthFlag, float documentTrueWidth) {
        List<Float> collect = elements.stream().map(PdfElement::getWidth).collect(Collectors.toList());
        //生成的列数量
        int columnNum = collect.size();
        //生成列表
        final PdfPTable pdfPTable = new PdfPTable(columnNum);
        pdfPTable.setSkipLastFooter(false);
        pdfPTable.setSkipFirstHeader(false);
        pdfPTable.setSpacingBefore(2);
        pdfPTable.setHorizontalAlignment(Element.ALIGN_LEFT);//设置内容水平居中显示
        pdfPTable.getDefaultCell().setBorderWidth(0);//边框 0
        float[] widths = new float[collect.size()];
        float width = 0f;
        for (int l = 0; l < columnNum; l++) {
            widths[l] = collect.get(l);
            width += collect.get(l);
        }
        if (widthFlag) {
            pdfPTable.setWidthPercentage(100);
        } else {
            pdfPTable.setWidthPercentage(width / documentTrueWidth * 100);
        }
        try {
            pdfPTable.setWidths(widths); //设置宽度比
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        for (PdfElement pdfElement : elements) {        //添加一行空的表格，方便调整格式
            PdfPCell tempCell = new PdfPCell();
            tempCell.setBorderWidth(0);
            pdfPTable.addCell(tempCell);
        }
        Float fitHeight = elements.stream().filter(i -> i.getHeight() > 0).map(PdfElement::getHeight).max(Float::compare).get();    //有效表格的高度
        for (PdfElement pdfElement : elements) {
            PdfPCell tempCell = null;
            Element element = pdfElement.getElement();
            if (element instanceof Phrase) {
                tempCell = new PdfPCell((Phrase) element);
            } else if (element instanceof Chunk) {
                Image image = ((Chunk) element).getImage();
                if (null == image) {
                    continue;
                }
                image.scaleToFit(pdfElement.getWidth(), pdfElement.getHeight());
                tempCell = new PdfPCell(image);
            }
            tempCell.setFixedHeight(fitHeight);
            tempCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tempCell.setVerticalAlignment(Element.ALIGN_MIDDLE);  // 设置垂直居中
            tempCell.setBorderWidth(0);
            pdfPTable.addCell(tempCell);
        }
        return pdfPTable;
    }
}
