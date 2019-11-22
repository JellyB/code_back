package jbzm.pdf;

import com.huatu.tiku.essay.util.file.PdfUtil;
import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;

public class TableTest {
    public static void main(String args[]) throws Exception {
        String path = "/users/zhengyi/Public/pdf/1.pdf";
        Document document = new Document(PageSize.A4, 45, 45, 100, 34);
        PdfUtil pdfUtil=new PdfUtil(path, document);
        document.open();
        pdfUtil.addForm(300,document);
        document.close();
    }
}
