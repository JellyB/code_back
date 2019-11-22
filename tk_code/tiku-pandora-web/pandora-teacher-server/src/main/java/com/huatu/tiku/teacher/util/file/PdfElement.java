package com.huatu.tiku.teacher.util.file;

import com.itextpdf.text.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by huangqingpeng on 2018/11/15.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PdfElement {
    private Element element;
    private float width;
    private float height;

    private String url;

    public PdfElement(Element element) {
        this.element = element;
        this.handlerSize();
    }

    public void handlerSize() {
        if (element instanceof Phrase) {
            String content = ((Phrase) element).getContent();
            Font font = ((Phrase) element).getFont();
            width = PDFDocument.getFontLength(content,font);
            height = font.getSize();
        } else if (element instanceof Image) {
            width = ((Image) element).getWidth();
            height = ((Image) element).getHeight();
        } else if(element instanceof Chunk){
            String content = ((Chunk) element).getContent();
            Font font = ((Chunk) element).getFont();
            width = PDFDocument.getFontLength(content,font);
            height = font.getSize();
        }
    }
}
