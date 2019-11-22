package com.huatu.ztk.backend.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

public class PDFHeaderFooter extends PdfPageEventHelper {

    private String papername;

    private String logopath; // 砖题库logo

    private String watermarkpath; // 水印图片路径

    private String header;

    private String footer;

    private PdfTemplate total;

    private Font bold_fontChinese;

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

        this.bold_fontChinese = bold_fontChinese;

    }

    public void setHeader(String header) {
        this.header = header;
    }

    public void setFooter(String footer) {

        this.footer = footer;

    }

    public void onOpenDocument(PdfWriter writer, Document document) {
        total = writer.getDirectContent().createTemplate(30, 16);
    }

    @Override
    public void onStartPage(PdfWriter writer, Document document) {

        try {

            // 添加水印图片
            PdfContentByte under = writer.getDirectContentUnder();

            // 添加水印图片
            Image image = Image.getInstance(watermarkpath);

            // image.setAbsolutePosition(150, 500);
            image.setAbsolutePosition(0, 0);

            under.addImage(image);

            // 添加水印文字

//            String waterMarkName = "华图教育";
//            BaseFont bfChinese =
//                    BaseFont.createFont("STSongStd-Light", "UniGB-UCS2-H",
//                            BaseFont.NOT_EMBEDDED);
//            under.beginText();
//            BaseColor textColor =
//                    new BaseColor(245, 245, 245);
//            under.setColorFill(textColor);
//            under.setFontAndSize(bfChinese, 100);
//            under.setTextMatrix(70, 0);
//
//            int rise = 200;
//            for (int k = 0; k < waterMarkName.length(); k++) {
//                under.setTextRise(rise);
//                char c = waterMarkName.charAt(k);
//                under.showText(c + " ");
//                rise += 100;
//            }
//
//            under.endText();


            document.add(new Paragraph(" "));

            Rectangle rect = writer.getBoxSize("art");

            // 汉字处理
            BaseFont bfChinese = BaseFont.createFont("STSongStd-Light",
                    "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);

            Font font = new Font(bfChinese, 16, Font.NORMAL, BaseColor.GRAY);

            // 页眉左边试卷名称
            ColumnText.showTextAligned(writer.getDirectContent(),
                    Element.ALIGN_LEFT, new Phrase(papername, font),
                    rect.getLeft(), rect.getTop() - 2, 0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {

        try {

            // 汉字处理
            BaseFont bfChinese = BaseFont.createFont("STSongStd-Light",
                    "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);

            Font font = new Font(bfChinese, 12, Font.NORMAL);

            Rectangle rect = writer.getBoxSize("art");

            ColumnText.showTextAligned(writer.getDirectContent(),
                    Element.ALIGN_RIGHT, new Phrase("tiku.huatu.com", font),
                    ((rect.getLeft() + rect.getRight()) / 2) + 30,
                    rect.getBottom() - 37, 0);

            ColumnText.showTextAligned(writer.getDirectContent(),
                    Element.ALIGN_RIGHT,
                    new Phrase(String.format("%d /", writer.getPageNumber()),
                            font), rect.getRight() - 35, rect.getBottom() - 38,
                    0);

            PdfPTable table = new PdfPTable(3);

            table.setSplitRows(false);

            // 页眉
            table.setWidths(new int[]{30, 30, 10});
            table.setTotalWidth(527);
            table.setLockedWidth(true);
            table.getDefaultCell().setFixedHeight(23);
            table.getDefaultCell().setBorder(Rectangle.BOTTOM);
            table.addCell(header);
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell("");

            // 砖题库图标
            Image logo = Image.getInstance(logopath);
            logo.setAlignment(Element.ALIGN_LEFT);
            PdfPCell cell = new PdfPCell(logo);
            cell.setBorder(Rectangle.BOTTOM);
            table.addCell(cell);

            // 页脚
            table.getDefaultCell().setFixedHeight(750);
            table.addCell(footer);
            table.addCell("");
            table.addCell("");
            /*
			 * PdfPCell cell1 = new PdfPCell(new Phrase());
			 * cell1.setBorder(Rectangle.TOP); table.addCell(cell1);
			 */

            // 总页数
			/*
			 * PdfPCell cellTotal = new PdfPCell(Image.getInstance(total));
			 * cellTotal.setFixedHeight(780);
			 * cellTotal.setBorder(Rectangle.BOTTOM); table.addCell(cellTotal);
			 */

            table.writeSelectedRows(0, -1, 34, 803, writer.getDirectContent());

            // 总页数

            writer.getDirectContent().addTemplate(total, document.right() - 20,
                    document.bottom() - 33);

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
            total.beginText();

            BaseFont bfont = BaseFont.createFont("STSongStd-Light",
                    "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);

            // Font font = new Font(bfChinese, 12, Font.NORMAL, BaseColor.GRAY);

            total.setFontAndSize(bfont, 12);

            total.setTextMatrix(0, 0);

            total.showText("" + (writer.getPageNumber() - 1));

            total.endText();

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
}
