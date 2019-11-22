package com.huatu.tiku.essay.util.file;


import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.io.IOException;

/**
 * Create by zhaoxi
 */
@Slf4j
public class DocUtil {


    public static final BaseFont BASEFONT = getBaseFont();
    public static Font CONTENT_FONT = new Font(BASEFONT, 11, Font.NORMAL, Color.BLACK);
    public static Font CONTENT_FONT_RED_UNDERLINE = new Font(BASEFONT, 10, Font.UNDERLINE, Color.RED);
    public static Font CONTENT_FONT_ORANGE_UNDERLINE = new Font(BASEFONT, 10, Font.UNDERLINE, Color.ORANGE);


    /**
     * 获取中文字体
     *
     * @return
     */
    private static BaseFont getBaseFont() {
        try {
            return  BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
        } catch (com.lowagie.text.DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    public static void correct(String str, Document document) throws Exception {
        char[] charList = str.toCharArray();

        Paragraph paragraph = new Paragraph();
        for (int i = 0; i < charList.length; i++) {
            if (charList[i] == '{' || charList[i] == '}' || charList[i] == '[' || charList[i] == ']' || charList[i] == '<' || charList[i] == '>') {
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
                                Phrase phrase = new Phrase(String.valueOf(charList[k]), CONTENT_FONT_ORANGE_UNDERLINE);
                                paragraph.add(phrase);
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
                        Phrase phrase = new Phrase(String.valueOf(charList[j]), CONTENT_FONT_ORANGE_UNDERLINE);
                        paragraph.add(phrase);
                    }
                }
            } else if (charList[i == 0 ? 0 : i - 1] == '[') {
                for (int j = i; j < charList.length; j++) {
                    if (charList[j] == ']') {
                        i = j;
                        break;
                    }
                    if ('{' != (charList[j]) && '}' != (charList[j])) {
                        Phrase phrase = new Phrase(String.valueOf(charList[j]), CONTENT_FONT_RED_UNDERLINE);
                        paragraph.add(phrase);
                    }
                }
            } else if (charList[i == 0 ? 0 : i - 1] == '{') {
                for (int j = i; j < charList.length; j++) {
                    if (charList[j] == '}') {
                        i = j;
                        break;
                    }
                    if ('{' != (charList[j]) && '}' != (charList[j])) {
                        Phrase phrase = new Phrase(String.valueOf(charList[j]), CONTENT_FONT_ORANGE_UNDERLINE);
                        paragraph.add(phrase);
                    }
                }
            } else {
                if ('{' != (charList[i]) && '}' != (charList[i])) {
                    Phrase phrase = new Phrase(String.valueOf(charList[i]), CONTENT_FONT);
                    paragraph.add(phrase);
                }
            }
        }


        document.add(paragraph);
    }

}
