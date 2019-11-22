package com.huatu.tiku.match.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class PDFTest {
    public final static String IMG_URL = "http://tiku.huatu.com/cdn/pandora/img/";//图片路径
    private final static String HEAD_QCODE_IMAGE_URL = IMG_URL + "question-qcode-back-ground.png";
    private final static String TAIL_QCODE_IMAGE_URL = IMG_URL + "answer-qcode-back-ground-1.png";
    private final static String TITLE_NAME = "";
    private static BaseFont bfChinese;
    private static Font titleFont;
    private static Font moduleFont;
    private static Font questionFont;
    private static Font baseFont;

    private static PdfWriter writer = null;
    private static Document document = null;

    static {
        try {
            bfChinese = BaseFont.createFont("/usr/share/fonts/simsun.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
            titleFont = new Font(bfChinese, 21, Font.BOLD);
            moduleFont = new Font(bfChinese, 18, Font.BOLD);
            questionFont = new Font(bfChinese, 11, Font.NORMAL);
            baseFont = new Font(bfChinese, 12, Font.NORMAL);
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        initPdf();
        writeTitle();
        close();
    }

    private static void initPdf() {
        String namePath = "/tmp/pdf/test.pdf";

        File file = new File(namePath);
        document = new Document(PageSize.A4, 36, 36, 40, 40);

        try {
            writer = PdfWriter.getInstance(document, new FileOutputStream(file));
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String headCode = HEAD_QCODE_IMAGE_URL;
        System.out.println("headCode = " + headCode);
        String tailCode = TAIL_QCODE_IMAGE_URL;
        int lenght = Math.min(30, TITLE_NAME.length());
        String headerName = "";
        if (TITLE_NAME.length() > lenght) {
            headerName = TITLE_NAME.substring(0, lenght) + "...";
        }
        PDFHeaderFooter header = new PDFHeaderFooter(headerName, headCode, tailCode, baseFont);
        writer.setBoxSize("art", PageSize.A4);
        writer.setPageEvent(header);
        //打开文件
        document.open();
    }

    public static void writeAlignCenter(String title, Float startY, PdfWriter writer, Font font) {
        int index = 0;
        while (index < title.length()) {
            int end = Math.min(index + 20, title.length());
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

    private static void writeTitle() {
        String title = "2019年0512深圳公务员考试《行测》真题";
        float y = document.top() - 150;      //纵坐标
        writeAlignCenter(title, y, writer, titleFont);
        document.newPage();
    }

    public static void close() {
        //添加尾页
        if (null != writer && null != document && document.isOpen()) {
            PDFHeaderFooter pageEvent = (PDFHeaderFooter) writer.getPageEvent();
            document.newPage();
            float y = document.top() - 150;      //纵坐标
            writeAlignCenter("查看参考答案与详细解析", y, writer, titleFont);
            int number = writer.getPageNumber();
            pageEvent.setPageNum(number);
            pageEvent.writeTailPage(writer);
        }
        if (null != document && document.isOpen()) {
            document.close();
        }
        if (null != writer) {
            writer.close();
        }
    }
}
