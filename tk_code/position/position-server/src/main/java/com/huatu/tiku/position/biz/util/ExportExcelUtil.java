package com.huatu.tiku.position.biz.util;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.List;

/**
 * @author wangjian
 **/
public class ExportExcelUtil {
    /**
     * 导出Excel
     * @param sheetName sheet名称
     * @param title 标题
     * @param values 内容
     * @return
     */
    public static HSSFWorkbook getHSSFWorkbook(String sheetName,String []title,List<List<String>> values ){

        // 第一步，创建一个HSSFWorkbook，对应一个Excel文件
        HSSFWorkbook wb = new HSSFWorkbook();

        // 第二步，在workbook中添加一个sheet,对应Excel文件中的sheet
        HSSFSheet sheet = wb.createSheet(sheetName);
        // 第三步，在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制
        HSSFRow row = sheet.createRow(0);
        row.setHeight((short) (25 * 20));//每行高度

        // 第四步，创建单元格，并设置值表头 设置表头居中
        HSSFCellStyle style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER_SELECTION); // 创建一个居中格式

        //声明列对象
        HSSFCell cell = null;
        //创建抬头
        for(int i=0;i<title.length;i++){
            cell = row.createCell(i);
            cell.setCellValue(title[i]);
            cell.setCellStyle(style);
        }



        //创建内容
        for(int i=0;i<values.size();i++){
            row = sheet.createRow(i + 2);
            row.setHeight((short) (25 * 20));//每行高度
            for(int j=0;j<values.get(i).size();j++){
                //将内容按顺序赋给对应的列对象
                cell = row.createCell(j);
                cell.setCellValue(values.get(i).get(j) );
                cell.setCellStyle(style);
            }
        }
        return wb;
    }
}
