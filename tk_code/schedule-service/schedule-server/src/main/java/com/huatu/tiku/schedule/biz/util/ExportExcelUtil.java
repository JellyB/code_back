package com.huatu.tiku.schedule.biz.util;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.util.CollectionUtils;

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
    public static HSSFWorkbook getHSSFWorkbook(String sheetName,String[] title,List<List<String>> values,String [] countStrings, Boolean exportClassHour){

        // 第一步，创建一个HSSFWorkbook，对应一个Excel文件
        HSSFWorkbook wb = new HSSFWorkbook();

        // 第二步，在workbook中添加一个sheet,对应Excel文件中的sheet
        HSSFSheet sheet = wb.createSheet(sheetName);
        sheet.setColumnWidth(0, 20 * 256);//每列宽度
        sheet.setColumnWidth(1, 20 * 256);//每列宽度
        sheet.setColumnWidth(2, 30 * 256);//每列宽度
        sheet.setColumnWidth(3, 20 * 256);//每列宽度
        sheet.setColumnWidth(4, 20 * 256);//每列宽度
        sheet.setColumnWidth(5, 30 * 256);//每列宽度
        sheet.setColumnWidth(6, 10 * 256);//每列宽度
        sheet.setColumnWidth(7, 10 * 256);//每列宽度
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));//合并标题单元格
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 2, 3));//合并标题单元格
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 4, 7));//合并标题单元格
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 7));//第二行
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 2));// 直播课
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 4, 6));// 线下课
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 7, 9));// 双师课
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 10, 11));// 地面班

        if (exportClassHour) {
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 12, 15));//合并标题单元格
        }

        sheet.addMergedRegion(new CellRangeAddress(5, 5, 0, 6));//合并标题单元格

        // 第三步，在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制
        HSSFRow row = sheet.createRow(0);
        row.setHeight((short) (25 * 20));//每行高度

        // 第四步，创建单元格，并设置值表头 设置表头居中
        HSSFCellStyle style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER_SELECTION); // 创建一个居中格式
        HSSFCellStyle style2 = wb.createCellStyle();
        style2.setAlignment(HorizontalAlignment.LEFT);

        //声明列对象
        HSSFCell cell = null;
        //创建抬头
        for(int i=0;i<title.length;i++){
            cell = row.createCell(2*i);
            cell.setCellValue(title[i]);
            cell.setCellStyle(style);
        }


        row = sheet.createRow(6);
        row.setHeight((short) (25 * 20));//每行高度
        //创建标题
        cell = row.createCell(0);
        cell.setCellValue("授课日期");
        cell.setCellStyle(style);
        cell = row.createCell(1);
        cell.setCellValue("授课时间");
        cell.setCellStyle(style);
        cell = row.createCell(2);
        cell.setCellValue("授课内容");
        cell.setCellStyle(style);
        cell = row.createCell(3);
        cell.setCellValue("授课教师");
        cell.setCellStyle(style);
        cell = row.createCell(4);
        cell.setCellValue("课程类型");
        cell.setCellStyle(style);
        cell = row.createCell(5);
        cell.setCellValue("所属课程");
        cell.setCellStyle(style);
        cell = row.createCell(6);
        cell.setCellValue("比例系数");
        cell.setCellStyle(style);
        cell = row.createCell(7);
        cell.setCellValue("课时数");
        cell.setCellStyle(style);

        HSSFRow row2 = sheet.createRow(1);
        row2.setHeight((short) (25 * 20));//每行高度
        HSSFCell cell2 =  row2.createCell(0);
        cell2.setCellValue("课时统计:");
        cell2.setCellStyle(style2);

        HSSFRow row3 = sheet.createRow(2);
        row3.setHeight((short) (25 * 20));//每行高度
        HSSFCell cell31 =  row3.createCell(0);
        cell31.setCellValue("直播课");
        cell31.setCellStyle(style);
        HSSFCell cell32=  row3.createCell(3);
        cell32.setCellValue("录播课");
        cell32.setCellStyle(style);
        HSSFCell cell33 =  row3.createCell(4);
        cell33.setCellValue("线下课");
        cell33.setCellStyle(style);
        HSSFCell cell35 =  row3.createCell(7);
        cell35.setCellValue("双师课");
        cell35.setCellStyle(style);
        HSSFCell cell36 =  row3.createCell(10);
        cell36.setCellValue("地面讲座");
        cell36.setCellStyle(style);

        if (exportClassHour) {
            HSSFCell cell34 =  row3.createCell(12);
            cell34.setCellValue("教研");
            cell34.setCellStyle(style);
        }

        HSSFRow row4 = sheet.createRow(3);
        row4.setHeight((short) (25 * 20));//每行高度
        HSSFCell cell41 =  row4.createCell(0);
        cell41.setCellValue("授课");
        cell41.setCellStyle(style);
        HSSFCell cell42 =  row4.createCell(1);
        cell42.setCellValue("练习");
        cell42.setCellStyle(style);
        HSSFCell cell43 =  row4.createCell(2);
        cell43.setCellValue("助教");
        cell43.setCellStyle(style);
        HSSFCell cell44 =  row4.createCell(3);
        cell44.setCellValue("录播反馈");
        cell44.setCellStyle(style);
        HSSFCell cell45 =  row4.createCell(4);
        cell45.setCellValue("授课");
        cell45.setCellStyle(style);
        HSSFCell cell46 =  row4.createCell(5);
        cell46.setCellValue("练习");
        cell46.setCellStyle(style);
        HSSFCell cell47 =  row4.createCell(6);
        cell47.setCellValue("助教");
        cell47.setCellStyle(style);
        HSSFCell cell48 =  row4.createCell(7);
        cell48.setCellValue("授课");
        cell48.setCellStyle(style);
        HSSFCell cell49 =  row4.createCell(8);
        cell49.setCellValue("练习");
        cell49.setCellStyle(style);
        HSSFCell cell410 =  row4.createCell(9);
        cell410.setCellValue("助教");
        cell410.setCellStyle(style);
        HSSFCell cell411 =  row4.createCell(10);
        cell411.setCellValue("授课");
        cell411.setCellStyle(style);
        HSSFCell cell412 =  row4.createCell(11);
        cell412.setCellValue("练习");
        cell412.setCellStyle(style);

        if (exportClassHour) {
            HSSFCell cell413 =  row4.createCell(12);
            cell413.setCellValue("真题解析");
            cell413.setCellStyle(style);
            HSSFCell cell414 =  row4.createCell(13);
            cell414.setCellValue("模拟题");
            cell414.setCellStyle(style);
            HSSFCell cell415 =  row4.createCell(14);
            cell415.setCellValue("教研文章");
            cell415.setCellStyle(style);
            HSSFCell cell416 =  row4.createCell(15);
            cell416.setCellValue("音频录制");
            cell416.setCellStyle(style);
        }

        HSSFRow row5 = sheet.createRow(4);
        row5.setHeight((short) (25 * 20));//每行高度
        HSSFCell cell50 =  row5.createCell(0);
        cell50.setCellValue(countStrings[0]);
        cell50.setCellStyle(style);
        HSSFCell cell51 =  row5.createCell(1);
        cell51.setCellValue(countStrings[1]);
        cell51.setCellStyle(style);
        HSSFCell cell52 =  row5.createCell(2);
        cell52.setCellValue(countStrings[13]);
        cell52.setCellStyle(style);
        HSSFCell cell53 =  row5.createCell(3);
        cell53.setCellValue(countStrings[6]);
        cell53.setCellStyle(style);
        HSSFCell cell54 =  row5.createCell(4);
        cell54.setCellValue(countStrings[2]);
        cell54.setCellStyle(style);
        HSSFCell cell55 =  row5.createCell(5);
        cell55.setCellValue(countStrings[3]);
        cell55.setCellStyle(style);
        HSSFCell cell56 =  row5.createCell(6);
        cell56.setCellValue(countStrings[14]);
        cell56.setCellStyle(style);
        HSSFCell cell57 =  row5.createCell(7);
        cell57.setCellValue(countStrings[9]);
        cell57.setCellStyle(style);
        HSSFCell cell58 =  row5.createCell(8);
        cell58.setCellValue(countStrings[10]);
        cell58.setCellStyle(style);
        HSSFCell cell59 =  row5.createCell(9);
        cell59.setCellValue(countStrings[15]);
        cell59.setCellStyle(style);
        HSSFCell cell510 =  row5.createCell(10);
        cell510.setCellValue(countStrings[11]);
        cell510.setCellStyle(style);
        HSSFCell cell511 =  row5.createCell(11);
        cell511.setCellValue(countStrings[12]);
        cell511.setCellStyle(style);

        if (exportClassHour) {
            HSSFCell cell512 =  row5.createCell(12);
            cell512.setCellValue(countStrings[4]);
            cell512.setCellStyle(style);
            HSSFCell cell513 =  row5.createCell(13);
            cell513.setCellValue(countStrings[5]);
            cell513.setCellStyle(style);
            HSSFCell cell514 =  row5.createCell(14);
            cell514.setCellValue(countStrings[7]);
            cell514.setCellStyle(style);
            HSSFCell cell515 =  row5.createCell(15);
            cell515.setCellValue(countStrings[8]);
            cell515.setCellStyle(style);
        }

        HSSFRow row6 = sheet.createRow(5);
        row6.setHeight((short) (25 * 20));//每行高度
        HSSFCell cell6 =  row6.createCell(0);
        cell6.setCellValue("课时明细:");
        cell6.setCellStyle(style2);

        //创建内容
        for(int i=0;i<values.size();i++){
            row = sheet.createRow(i + 7);
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

    public static HSSFWorkbook downLoadRankExcel(String sheetName, List<String> parameters, List<List<String>> values ){

        // 第一步，创建一个HSSFWorkbook，对应一个Excel文件
        HSSFWorkbook wb = new HSSFWorkbook();

        // 第二步，在workbook中添加一个sheet,对应Excel文件中的sheet
        HSSFSheet sheet = wb.createSheet(sheetName);

        int columnIndex = 0;

        sheet.setColumnWidth(columnIndex++, 10 * 256);//每列宽度
        sheet.setColumnWidth(columnIndex++, 20 * 256);//每列宽度
        sheet.setColumnWidth(columnIndex++, 10 * 256);//每列宽度
        if (CollectionUtils.isEmpty(parameters) || parameters.contains("1")) {
            sheet.setColumnWidth(columnIndex++, 10 * 256);//每列宽度
        }
        if (CollectionUtils.isEmpty(parameters) || parameters.contains("2")) {
            sheet.setColumnWidth(columnIndex++, 10 * 256);//每列宽度
        }
        if (CollectionUtils.isEmpty(parameters) || parameters.contains("3")) {
            sheet.setColumnWidth(columnIndex++, 10 * 256);//每列宽度
        }
        if (CollectionUtils.isEmpty(parameters) || parameters.contains("4")) {
            sheet.setColumnWidth(columnIndex++, 10 * 256);//每列宽度
        }
        if (CollectionUtils.isEmpty(parameters) || parameters.contains("5")) {
            sheet.setColumnWidth(columnIndex++, 10 * 256);//每列宽度
        }
        if (CollectionUtils.isEmpty(parameters) || parameters.contains("6")) {
            sheet.setColumnWidth(columnIndex++, 10 * 256);//每列宽度
        }
        if (CollectionUtils.isEmpty(parameters) || parameters.contains("7")) {
            sheet.setColumnWidth(columnIndex++, 10 * 256);//每列宽度
        }
        if (CollectionUtils.isEmpty(parameters) || parameters.contains("8")) {
            sheet.setColumnWidth(columnIndex++, 10 * 256);//每列宽度
        }
        if (CollectionUtils.isEmpty(parameters) || parameters.contains("9")) {
            sheet.setColumnWidth(columnIndex++, 10 * 256);//每列宽度
        }
        if (CollectionUtils.isEmpty(parameters) || parameters.contains("10")) {
            sheet.setColumnWidth(columnIndex++, 10 * 256);//每列宽度
        }
        if (CollectionUtils.isEmpty(parameters) || parameters.contains("11")) {
            sheet.setColumnWidth(columnIndex++, 10 * 256);//每列宽度
        }
        if (CollectionUtils.isEmpty(parameters) || parameters.contains("12")) {
            sheet.setColumnWidth(columnIndex++, 10 * 256);//每列宽度
        }
        if (CollectionUtils.isEmpty(parameters) || parameters.contains("13")) {
            sheet.setColumnWidth(columnIndex++, 10 * 256);//每列宽度
        }

        // 第三步，在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制
        HSSFRow row = sheet.createRow(0);
        row.setHeight((short) (25 * 20));//每行高度

        // 第四步，创建单元格，并设置值表头 设置表头居中
        HSSFCellStyle style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER_SELECTION); // 创建一个居中格式

        //声明列对象
        HSSFCell cell = null;

        int cellColumnIndex = 0;

        //创建标题
        cell = row.createCell(cellColumnIndex++);
        cell.setCellValue("序号");
        cell.setCellStyle(style);
        cell = row.createCell(cellColumnIndex++);
        cell.setCellValue("教师姓名");
        cell.setCellStyle(style);
        cell = row.createCell(cellColumnIndex++);
        cell.setCellValue("课时总计");
        cell.setCellStyle(style);
        if (CollectionUtils.isEmpty(parameters) || parameters.contains("1")) {
            cell = row.createCell(cellColumnIndex++);
            cell.setCellValue("直播授课");
            cell.setCellStyle(style);
        }
        if (CollectionUtils.isEmpty(parameters) || parameters.contains("2")) {
            cell = row.createCell(cellColumnIndex++);
            cell.setCellValue("录播授课");
            cell.setCellStyle(style);
        }
        if (CollectionUtils.isEmpty(parameters) || parameters.contains("3")) {
            cell = row.createCell(cellColumnIndex++);
            cell.setCellValue("线下授课");
            cell.setCellStyle(style);
        }
        if (CollectionUtils.isEmpty(parameters) || parameters.contains("4")) {
            cell = row.createCell(cellColumnIndex++);
            cell.setCellValue("线下分校授课");
            cell.setCellStyle(style);
        }
        if (CollectionUtils.isEmpty(parameters) || parameters.contains("5")) {
            cell = row.createCell(cellColumnIndex++);
            cell.setCellValue("线上线下练习");
            cell.setCellStyle(style);
        }
        if (CollectionUtils.isEmpty(parameters) || parameters.contains("6")) {
            cell = row.createCell(cellColumnIndex++);
            cell.setCellValue("线上助教");
            cell.setCellStyle(style);
        }
        if (CollectionUtils.isEmpty(parameters) || parameters.contains("7")) {
            cell = row.createCell(cellColumnIndex++);
            cell.setCellValue("线下助教");
            cell.setCellStyle(style);
        }
        if (CollectionUtils.isEmpty(parameters) || parameters.contains("8")) {
            cell = row.createCell(cellColumnIndex++);
            cell.setCellValue("教研模拟题");
            cell.setCellStyle(style);
        }
        if (CollectionUtils.isEmpty(parameters) || parameters.contains("9")) {
            cell = row.createCell(cellColumnIndex++);
            cell.setCellValue("教研真题解析");
            cell.setCellStyle(style);
        }
        if (CollectionUtils.isEmpty(parameters) || parameters.contains("10")) {
            cell = row.createCell(cellColumnIndex++);
            cell.setCellValue("教研文章");
            cell.setCellStyle(style);
        }
        if (CollectionUtils.isEmpty(parameters) || parameters.contains("11")) {
            cell = row.createCell(cellColumnIndex++);
            cell.setCellValue("教研音频录制");
            cell.setCellStyle(style);
        }
        if (CollectionUtils.isEmpty(parameters) || parameters.contains("12")) {
            cell = row.createCell(cellColumnIndex++);
            cell.setCellValue("双师课授课");
            cell.setCellStyle(style);
        }
        if (CollectionUtils.isEmpty(parameters) || parameters.contains("13")) {
            cell = row.createCell(cellColumnIndex++);
            cell.setCellValue("地面讲座授课");
            cell.setCellStyle(style);
        }

        //创建内容
        for(int i=0;i<values.size();i++){
            row = sheet.createRow(i + 1);
            row.setHeight((short) (25 * 20));//每行高度

            int contentCellColumnIndex = 0;

            cell = row.createCell(contentCellColumnIndex);
            cell.setCellValue(values.get(i).get(contentCellColumnIndex++) );
            cell.setCellStyle(style);

            cell = row.createCell(contentCellColumnIndex);
            cell.setCellValue(values.get(i).get(contentCellColumnIndex++));
            cell.setCellStyle(style);

            cell = row.createCell(contentCellColumnIndex);
            cell.setCellValue(values.get(i).get(contentCellColumnIndex++));
            cell.setCellStyle(style);

            if (CollectionUtils.isEmpty(parameters) || parameters.contains("1")) {
                cell = row.createCell(contentCellColumnIndex++);
                cell.setCellValue(values.get(i).get(3));
                cell.setCellStyle(style);
            }
            if (CollectionUtils.isEmpty(parameters) || parameters.contains("2")) {
                cell = row.createCell(contentCellColumnIndex++);
                cell.setCellValue(values.get(i).get(4));
                cell.setCellStyle(style);
            }
            if (CollectionUtils.isEmpty(parameters) || parameters.contains("3")) {
                cell = row.createCell(contentCellColumnIndex++);
                cell.setCellValue(values.get(i).get(5));
                cell.setCellStyle(style);
            }
            if (CollectionUtils.isEmpty(parameters) || parameters.contains("4")) {
                cell = row.createCell(contentCellColumnIndex++);
                cell.setCellValue(values.get(i).get(6));
                cell.setCellStyle(style);
            }
            if (CollectionUtils.isEmpty(parameters) || parameters.contains("5")) {
                cell = row.createCell(contentCellColumnIndex++);
                cell.setCellValue(values.get(i).get(7));
                cell.setCellStyle(style);
            }
            if (CollectionUtils.isEmpty(parameters) || parameters.contains("6")) {
                cell = row.createCell(contentCellColumnIndex++);
                cell.setCellValue(values.get(i).get(8));
                cell.setCellStyle(style);
            }
            if (CollectionUtils.isEmpty(parameters) || parameters.contains("7")) {
                cell = row.createCell(contentCellColumnIndex++);
                cell.setCellValue(values.get(i).get(9));
                cell.setCellStyle(style);
            }
            if (CollectionUtils.isEmpty(parameters) || parameters.contains("8")) {
                cell = row.createCell(contentCellColumnIndex++);
                cell.setCellValue(values.get(i).get(10));
                cell.setCellStyle(style);
            }
            if (CollectionUtils.isEmpty(parameters) || parameters.contains("9")) {
                cell = row.createCell(contentCellColumnIndex++);
                cell.setCellValue(values.get(i).get(11));
                cell.setCellStyle(style);
            }
            if (CollectionUtils.isEmpty(parameters) || parameters.contains("10")) {
                cell = row.createCell(contentCellColumnIndex++);
                cell.setCellValue(values.get(i).get(12));
                cell.setCellStyle(style);
            }
            if (CollectionUtils.isEmpty(parameters) || parameters.contains("11")) {
                cell = row.createCell(contentCellColumnIndex++);
                cell.setCellValue(values.get(i).get(13));
                cell.setCellStyle(style);
            }
            if (CollectionUtils.isEmpty(parameters) || parameters.contains("12")) {
                cell = row.createCell(contentCellColumnIndex++);
                cell.setCellValue(values.get(i).get(14));
                cell.setCellStyle(style);
            }
            if (CollectionUtils.isEmpty(parameters) || parameters.contains("13")) {
                cell = row.createCell(contentCellColumnIndex++);
                cell.setCellValue(values.get(i).get(15));
                cell.setCellStyle(style);
            }
        }
        return wb;
    }
}
