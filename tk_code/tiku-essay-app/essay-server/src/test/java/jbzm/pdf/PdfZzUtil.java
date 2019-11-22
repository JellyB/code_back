package jbzm.pdf;

import com.itextpdf.text.*;
import jbzm.pdf.PdfUtil;
import com.itextpdf.text.pdf.*;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.UUID;


import java.util.Date;
import java.lang.*;

import static com.itextpdf.text.Element.PHRASE;

public class PdfZzUtil {
    public void correct(String path,String str) throws Exception {
        long startTime = new Date().getTime();
        Document document = new Document(PageSize.A4, 45, 45, 100, 34);
        PdfUtil pdf = new PdfUtil(path, document);
        document.open();
        char[] charList = str.toCharArray();
        Paragraph paragraph = new Paragraph();
        for (int i = 0; i < charList.length; i++) {
            if (charList[i] == '[' || charList[i] == ']' || charList[i] == '<' || charList[i] == '>') {
                continue;
            } else if (i > 1 && charList[i - 1] == '[') {
                for (int j = i; j < charList.length; j++) {
                    if (charList[j] == ']') {
                        i = j;
                        break;
                    }
                    Phrase phrase = new Phrase(String.valueOf(charList[j]), PdfUtil.CONTENT_FONT_RED);
                    paragraph.add(phrase);
                }
            } else if (i > 1 && charList[i - 1] == '<') {
                for (int j = i; j < charList.length; j++) {
                    if (charList[j] == '>') {
                        i = j;
                        break;
                    }
                    Phrase phrase = new Phrase(String.valueOf(charList[j]), PdfUtil.CONTENT_FONT_ORANGE);
                    paragraph.add(phrase);
                }
            } else {
                Phrase phrase = new Phrase(String.valueOf(charList[i]), PdfUtil.CONTENT_FONT);
                paragraph.add(phrase);
            }
        }
        document.add(paragraph);
        document.close();
        long endTime = new Date().getTime();
        System.out.println(endTime-startTime);
    }
}
