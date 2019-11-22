package com.huatu.ztk.backend.paperUpload.dao;

import org.apache.poi.xwpf.usermodel.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * Created by lenovo on 2017/4/22.
 */
public class XwpfDao {

    public void testReadByDoc() throws Exception {
        try (InputStream is = new FileInputStream("D:\\table.docx")) {
            XWPFDocument doc = new XWPFDocument(is);
            List<XWPFParagraph> paras = doc.getParagraphs();

            for (XWPFParagraph para : paras) {
                //当前段落的属性
    //       CTPPr pr = para.getCTP().getPPr();
                System.out.println(para.getText());
            }
            //获取文档中所有的表格
            List<XWPFTable> tables = doc.getTables();
            List<XWPFTableRow> rows;
            List<XWPFTableCell> cells;
            for (XWPFTable table : tables) {
                //表格属性
    //       CTTblPr pr = table.getCTTbl().getTblPr();
                //获取表格对应的行
                rows = table.getRows();
                for (XWPFTableRow row : rows) {
                    //获取行对应的单元格
                    cells = row.getTableCells();
                    for (XWPFTableCell cell : cells) {
                        System.out.println(cell.getText());
                        ;
                    }
                }
            }
            this.close(is);
        }
    }

    /**
     * 关闭输入流
     * @param is
     */
    private void close(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
