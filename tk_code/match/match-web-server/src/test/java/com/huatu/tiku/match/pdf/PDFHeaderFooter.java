package com.huatu.tiku.match.pdf;


import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.IOException;

public class PDFHeaderFooter extends PdfPageEventHelper {

    private String papername;

    private String logopath; // 砖题库logo

    private String watermarkpath; // 水印图片路径

    public static final String waterQuestionPath = "http://tiku.huatu.com/cdn/paper/pdf/water-question.png";

    private String header;

    private String footer;

    private PdfTemplate pdfTemplate;

    private Font bold_fontChinese;

    private int pageNum = 1;

    private static final float demo_2_real_pdf_percent = 0.24f;

    public PDFHeaderFooter() {
    }

    /**
     * @param papername     试卷名称
     * @param logopath      砖题库图标
     * @param watermarkpath 页眉图片路径
     */
    public PDFHeaderFooter(String papername, String logopath,
                           String watermarkpath, Font bold_fontChinese) {

        this.papername = papername;

        this.logopath = logopath;

        this.watermarkpath = watermarkpath;

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
            image.scaleAbsolute(2032 * demo_2_real_pdf_percent, 776 * demo_2_real_pdf_percent);
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
     */
    public void writeTailPage(PdfWriter writer) {
        try {
            writer.newPage();
            // 添加水印图片
            PdfContentByte under = writer.getDirectContentUnder();
            // 添加水印图片
            Image image = Image.getInstance(watermarkpath);
            image.setScaleToFitLineWhenOverflow(true);
            image.setScaleToFitHeight(true);
            image.setAlignment(Image.TEXTWRAP);
            image.scaleAbsolute(2032 * demo_2_real_pdf_percent, 1133 * demo_2_real_pdf_percent);
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

    @Override
    public void onStartPage(PdfWriter writer, Document document) {
        try {
            /**
             * 页眉
             */
            ColumnText.showTextAligned(writer.getDirectContent(),
                    Element.ALIGN_LEFT, new Phrase(papername, bold_fontChinese),
                    document.left(),
                    document.top(-15), 0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        try {

            Anchor anchor = new Anchor(new Chunk("华图在线官方网址 ", bold_fontChinese));
            anchor.setReference("http://v.huatu.com");
            BaseFont baseFont = BaseFont.createFont();
            Font font = new Font(baseFont, 12);
            font.setColor(BaseColor.LIGHT_GRAY);
            Anchor anchor1 = new Anchor(new Chunk("http://v.huatu.com ", font));
            anchor.setReference("http://v.huatu.com");
            BaseFont calculatedBaseFont = bold_fontChinese.getCalculatedBaseFont(false);
            float widthPoint = calculatedBaseFont.getWidthPoint("华图在线官方网址 ", bold_fontChinese.getSize());
            float y = -25;          //页脚字表到段落的间距
            float splitSpace = 10;          //段落与页眉页脚线之间的间距
            PdfPTable table = new PdfPTable(1);
            table.setTotalWidth(document.right() - document.left());
            PdfPCell cell = new PdfPCell();
            cell.setBorderWidthTop(0.1f);
            cell.setBorderWidthBottom(0.1f);
            cell.setBorderWidthLeft(0f);
            cell.setBorderWidthRight(0f);
            cell.setUseBorderPadding(false);
            cell.setFixedHeight(document.top() - document.bottom() + splitSpace * 2);
            cell.setBorderColorTop(BaseColor.BLACK);
            cell.setBorderColorBottom(BaseColor.BLACK);
//            cell.setBackgroundColor(BaseColor.BLUE);
            table.addCell(cell);
            table.writeSelectedRows(0, -1, document.left(), document.top(-splitSpace), writer.getDirectContent());
            /**
             * 页脚
             */
            ColumnText.showTextAligned(writer.getDirectContent(),
                    Element.ALIGN_LEFT, anchor,
                    document.left(),
                    document.bottom(y), 0);
            ColumnText.showTextAligned(writer.getDirectContent(),
                    Element.ALIGN_LEFT, anchor1,
                    document.left(widthPoint),
                    document.bottom(y), 0);
            int pageNumber = writer.getPageNumber();
            ColumnText.showTextAligned(writer.getDirectContent(),
                    Element.ALIGN_RIGHT,
                    new Phrase(String.format("第%d页", pageNumber),
                            bold_fontChinese), document.right(38), document.bottom(y),
                    0);

            // 总页数
            writer.getDirectContent().addTemplate(pdfTemplate, document.right(38),
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

            pdfTemplate.setFontAndSize(bold_fontChinese.getBaseFont(), bold_fontChinese.getSize());
            pdfTemplate.setTextMatrix(0, 0);
            pdfTemplate.setColorFill(BaseColor.LIGHT_GRAY);
            pdfTemplate.showText("共" + (writer.getPageNumber() - 1) + "页");

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

    public void waterImage(Document document, PdfWriter writer) throws Exception {
        int pageNumber = writer.getPageNumber();
        //除了第一页其他页的斜体字水印
        if (pageNumber == 1 || pageNumber == pageNum) {
            return;
        }
        PdfContentByte waterMar = writer.getDirectContent();
        waterMar.beginText();
        PdfGState gs = new PdfGState();
        gs.setStrokeOpacity(0f);
        try {
            Image image = Image.getInstance(waterQuestionPath);
            image.scaleAbsolute(200, 100);//大小
            image.setAbsolutePosition(document.left(80), document.bottom(80));
            waterMar.addImage(image);
            image.setAbsolutePosition(document.left(80), document.top(180));
            waterMar.addImage(image);
            image.setAbsolutePosition(document.left(300), (document.top() + document.bottom()) / 2);
            waterMar.addImage(image);
            waterMar.endText();
            waterMar.stroke();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            waterMar = null;
            gs = null;
        }
    }

    public void waterText(Document document, PdfWriter writer) throws Exception {
        int pageNumber = writer.getPageNumber();
        //除了第一页其他页的斜体字水印
        if (pageNumber == 1 || pageNumber == pageNum) {
            return;
        }
        PdfContentByte waterMar = writer.getDirectContent();
        waterMar.beginText();
        PdfGState gs = new PdfGState();
        gs.setFillOpacity(0.2f);
        try {
            float rotation = 30f;
            waterMar.setFontAndSize(BaseFont.createFont(), 50);
            waterMar.setGState(gs);
            waterMar.showTextAlignedKerned(Element.ALIGN_LEFT, "v.huatu.com", document.left(80), document.bottom(80), rotation);
            waterMar.showTextAlignedKerned(Element.ALIGN_LEFT, "v.huatu.com", document.left(80), document.top(200), rotation);
            waterMar.showTextAlignedKerned(Element.ALIGN_LEFT, "v.huatu.com", document.left(260), (document.top() + document.bottom()) / 2, rotation);
            waterMar.setColorFill(BaseColor.GRAY);
            waterMar.endText();
            waterMar.stroke();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            waterMar = null;
            gs = null;
        }
    }
}
