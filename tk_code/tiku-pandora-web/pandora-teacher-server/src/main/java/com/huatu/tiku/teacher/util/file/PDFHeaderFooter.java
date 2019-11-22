package com.huatu.tiku.teacher.util.file;

import com.huatu.tiku.teacher.service.impl.download.v1.PdfWriteServiceImplV1;
import com.huatu.tiku.util.file.FunFileUtils;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.IOException;

public class PDFHeaderFooter extends PdfPageEventHelper {

    private String papername;

    private String logopath; // 砖题库logo


    private String tailPath;

    public static final String waterQuestionPath = FunFileUtils.PDF_FILE_SAVE_URL + "water-question.png";
    public static final String headerImagePath = "http://tiku.huatu.com/cdn/pandora/logo3.png";

    private String header;

    private String footer;

    private PdfTemplate pdfTemplate;

    private Font bold_fontChinese;

    private int pageNum = 1;

    private static final float demo_2_real_pdf_percent = 0.24f;

    private static final int DOCUMENT_QCODE_WIDTH = 2032;

    static Image headerImage = null;

    private boolean footFlag = true;

    public void setFootFlag(boolean footFlag) {
        this.footFlag = footFlag;
    }

    /**
     * @param papername 试卷名称
     * @param logopath  砖题库图标
     */
    public PDFHeaderFooter(String papername, String logopath, String tailPath, Font bold_fontChinese) {

        this.papername = papername;

        this.logopath = logopath;

        this.tailPath = tailPath;

        bold_fontChinese.setColor(BaseColor.LIGHT_GRAY);
        this.bold_fontChinese = bold_fontChinese;

    }

    public void setHeader(String header) {
        this.header = header;
    }

    public void setFooter(String footer) {

        this.footer = footer;

    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public void onOpenDocument(PdfWriter writer, Document document) {
        pdfTemplate = writer.getDirectContent().createTemplate(36, 16);
        writeHeadPage(writer);
    }

    /**
     * 首页图片插入
     *
     * @param writer
     */
    public void writeHeadPage(PdfWriter writer) {
        try {
            // 添加水印图片
            PdfContentByte under = writer.getDirectContentUnder();
            // 添加水印图片
            Image image = Image.getInstance(logopath);
            image.setScaleToFitLineWhenOverflow(true);
            image.setScaleToFitHeight(true);
            image.scaleAbsolute(DOCUMENT_QCODE_WIDTH * demo_2_real_pdf_percent, 776 * demo_2_real_pdf_percent);
            float absoluteX = 220 * demo_2_real_pdf_percent;
            float absoluteY = 318 * demo_2_real_pdf_percent;

            image.setAbsolutePosition(absoluteX, absoluteY);
            under.addImage(image);
        } catch (BadElementException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * 首页图片插入
     *
     * @param writer
     * @param document
     * @param pdfWriteTool
     */
    public void writeTailPage(PdfWriter writer, Document document, PdfWriteServiceImplV1.PdfWriteTool pdfWriteTool) {
        try {
            writer.newPage();
            // 添加水印图片
            PdfContentByte under = writer.getDirectContentUnder();
            // 添加水印图片
            Image image = Image.getInstance(tailPath);
            image.setScaleToFitLineWhenOverflow(true);
            image.setScaleToFitHeight(true);
            image.setAlignment(Image.TEXTWRAP);
            float width = document.right() - document.left();
            image.scaleAbsolute(width, image.getPlainHeight() * width / image.getPlainWidth());
            int subject = pdfWriteTool.getSubject();
            Font boldFont = new Font(pdfWriteTool.getBfChinese(), 14, Font.BOLD);
            Font normalFont = new Font(pdfWriteTool.getBfChinese(), 14, Font.NORMAL);
            if (subject == 1) {
                Phrase phrase1 = new Phrase();
                Chunk chunk1 = new Chunk("华图在线搜索：", normalFont);
                Chunk chunk2 = new Chunk("系统提分班", boldFont);
                phrase1.add(chunk1);
                phrase1.add(chunk2);
                Phrase phrase2 = new Phrase();
                Chunk chunk3 = new Chunk("名师授课 超高性价比 一站式满足公务员备考所有需求", normalFont);
                phrase2.add(chunk3);
                ColumnText.showTextAligned(writer.getDirectContent(),
                        Element.ALIGN_CENTER, phrase1,
                        PageSize.A4.getWidth() / 2, document.bottom() + 2 * boldFont.getSize(), 0);
                ColumnText.showTextAligned(writer.getDirectContent(),
                        Element.ALIGN_CENTER, phrase2,
                        PageSize.A4.getWidth() / 2, document.bottom(), 0);
            }
            image.setAbsolutePosition(document.left(), document.bottom() + (subject == 1 ? 5 : 2) * normalFont.getSize());
            under.addImage(image);
        } catch (BadElementException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStartPage(PdfWriter writer, Document document) {
        try {
            /**
             * 页眉
             */
//            ColumnText.showTextAligned(writer.getDirectContent(),
//                    Element.ALIGN_LEFT, new Phrase(papername, bold_fontChinese),
//                    document.left(),
//                    document.top(-15), 0);
            if (null == headerImage) {
                headerImage = Image.getInstance(headerImagePath);
            }
//            headerImage.setWidthPercentage(width / headerImage.getPlainWidth() * 100);
            headerImage.setAlignment(Image.UNDERLYING);
            headerImage.scaleAbsolute(document.right() - document.left(), 712);
            headerImage.setAbsolutePosition(document.left(), document.top() - 712 + 70);
            document.add(headerImage);
//            writer.getDirectContent().addImage(headerImage, width, 0, 0,
//                    width / headerImage.getPlainWidth() * headerImage.getPlainHeight(),
//                    document.left(), document.top() + 30);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {

        try {

//            Anchor anchor = new Anchor(new Chunk("华图在线官方网址 ", bold_fontChinese));
//            anchor.setReference("http://v.huatu.com");
//            BaseFont baseFont = BaseFont.createFont();
//            Font font = new Font(baseFont, 12);
//            font.setColor(BaseColor.LIGHT_GRAY);
//            Anchor anchor1 = new Anchor(new Chunk("http://v.huatu.com ", font));
//            anchor.setReference("http://v.huatu.com");
//            BaseFont calculatedBaseFont = bold_fontChinese.getCalculatedBaseFont(false);
//            float widthPoint = calculatedBaseFont.getWidthPoint("华图在线官方网址 ", bold_fontChinese.getSize());
            float y = -25;          //页脚字表到段落的间距
//            float splitSpace = 10;          //段落与页眉页脚线之间的间距
//            PdfPTable table = new PdfPTable(1);
//            table.setTotalWidth(document.right() - document.left());
//            PdfPCell cell = new PdfPCell();
//            cell.setBorderWidthTop(0.1f);
//            cell.setBorderWidthBottom(0f);
//            cell.setBorderWidthLeft(0f);
//            cell.setBorderWidthRight(0f);
//            cell.setUseBorderPadding(false);
//            cell.setFixedHeight(document.top() - document.bottom() + splitSpace * 2 + 30);
////            cell.setFixedHeight(document.top() - document.bottom());
//            cell.setBorderColorTop(BaseColor.BLACK);
////            cell.setBackgroundColor(BaseColor.BLUE);
//            table.addCell(cell);
//            table.writeSelectedRows(0, -1, document.left(), document.top(-(splitSpace + 30)), writer.getDirectContent());
            /**
             * 页脚
             //             */
//            ColumnText.showTextAligned(writer.getDirectContent(),
//                    Element.ALIGN_LEFT, anchor,
//                    document.left(),
//                    document.bottom(y), 0);
//            ColumnText.showTextAligned(writer.getDirectContent(),
//                    Element.ALIGN_LEFT, anchor1,
//                    document.left(widthPoint),
//                    document.bottom(y), 0);
            int pageNumber = writer.getPageNumber();
            if (pageNumber == 1 || pageNumber == pageNum) {
                return;
            }
            float location = document.left() + (document.right() - document.left()) / 2;
            Font font = new Font(bold_fontChinese.getBaseFont(), 10, bold_fontChinese.getStyle(), BaseColor.GRAY);
            ColumnText.showTextAligned(writer.getDirectContent(),
                    Element.ALIGN_RIGHT,
                    new Phrase(String.format("%d/", pageNumber - 1),
                            font), location, document.bottom(y),
                    0);

            // 总页数
            writer.getDirectContent().addTemplate(pdfTemplate, location,
                    document.bottom(y));
//            //除了第一页其他页的斜体字水印(必须放到页面内容都加载完成后再添加水印，不然子题会整体变灰)
//            if (pageNumber != 1 && pageNumber != pageNum) {
//                waterText(document, writer);
//            }
        } catch (Exception ex) {

            ex.printStackTrace();

        }

    }

    @Override
    public void onCloseDocument(PdfWriter writer, Document document) {
        /*
         * ColumnText .showTextAligned(total, Element.ALIGN_LEFT, new
         * Phrase(String.valueOf(writer.getPageNumber() - 1)), 2, 2, 0);
         */

        try {
//            writeTailPage(writer);
            pdfTemplate.beginText();

            pdfTemplate.setFontAndSize(bold_fontChinese.getBaseFont(), 10);
            pdfTemplate.setTextMatrix(0, 0);
            pdfTemplate.setColorFill(BaseColor.GRAY);
            pdfTemplate.showText((writer.getPageNumber() - (footFlag ? 3 : 2)) + "");

            pdfTemplate.endText();

        } catch (Exception ex) {

            ex.printStackTrace();

        }

    }

    @Override
    public void onParagraph(PdfWriter writer, Document document,
                            float paragraphPosition) {

        /*
         * if (paragraphPosition < 130) {
         *
         * document.newPage(); }
         */

    }

    @Override
    public void onParagraphEnd(PdfWriter writer, Document document,
                               float paragraphPosition) {

    }

//    public void waterImage(Document document, PdfWriter writer) throws Exception {
//        int pageNumber = writer.getPageNumber();
//        //除了第一页其他页的斜体字水印
//        if (pageNumber == 1 || pageNumber == pageNum) {
//            return;
//        }
//        PdfContentByte waterMar = writer.getDirectContent();
//        waterMar.beginText();
//        PdfGState gs = new PdfGState();
//        gs.setStrokeOpacity(0f);
//        try {
//            Image image = Image.getInstance(waterQuestionPath);
//            image.scaleAbsolute(200, 100);//大小
//            image.setAbsolutePosition(document.left(80), document.bottom(80));
//            waterMar.addImage(image);
//            image.setAbsolutePosition(document.left(80), document.top(180));
//            waterMar.addImage(image);
//            image.setAbsolutePosition(document.left(300), (document.top() + document.bottom()) / 2);
//            waterMar.addImage(image);
//            waterMar.endText();
//            waterMar.stroke();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            waterMar = null;
//            gs = null;
//        }
//    }
//
//    public void waterText(Document document, PdfWriter writer) throws Exception {
//        int pageNumber = writer.getPageNumber();
//        //除了第一页其他页的斜体字水印
//        if (pageNumber == 1 || pageNumber == pageNum) {
//            return;
//        }
//        PdfContentByte waterMar = writer.getDirectContent();
//        waterMar.beginText();
//        PdfGState gs = new PdfGState();
//        gs.setFillOpacity(0.2f);
//        try {
//            float rotation = 30f;
//            waterMar.setFontAndSize(BaseFont.createFont(), 50);
//            waterMar.setGState(gs);
//            waterMar.showTextAlignedKerned(Element.ALIGN_LEFT, "v.huatu.com", document.left(80), document.bottom(80), rotation);
//            waterMar.showTextAlignedKerned(Element.ALIGN_LEFT, "v.huatu.com", document.left(80), document.top(200), rotation);
//            waterMar.showTextAlignedKerned(Element.ALIGN_LEFT, "v.huatu.com", document.left(260), (document.top() + document.bottom()) / 2, rotation);
//            waterMar.setColorFill(BaseColor.GRAY);
//            waterMar.endText();
//            waterMar.stroke();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            waterMar = null;
//            gs = null;
//        }
//    }
}
