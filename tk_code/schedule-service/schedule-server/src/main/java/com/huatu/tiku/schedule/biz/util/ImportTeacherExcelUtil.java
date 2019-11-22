package com.huatu.tiku.schedule.biz.util;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by duanxiangchao on 2018/5/11
 */
public class ImportTeacherExcelUtil {

    private int totalRows = 0;

    private int totalCells = 0;

    public int getTotalCells() {
        return totalCells;
    }


    /**
     * 根据流读取Excel文件
     * @param inputStream
     * @param isExcel2003
     * @return
     */
    public List<List<List<String>>> read(InputStream inputStream, boolean isExcel2003) {
        List<List<List<String>>> dataLst = new ArrayList<List<List<String>>>();;
        Workbook wb = null;
        try {
            /** 根据版本选择创建Workbook的方式 */
            if (isExcel2003) {
                wb = new HSSFWorkbook(inputStream);
            } else {
                wb = new XSSFWorkbook(inputStream);
            }

            int sheetNumber = wb.getNumberOfSheets();
            for(int i = 0; i < sheetNumber; i++){
                Sheet sheet = wb.getSheetAt(i);
                dataLst.add(read(sheet));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataLst;
    }

    /**
     * @description: sheet页转 List<List<String>>
     * @author duanxiangchao
     * @date 2018/5/16 下午3:27
     */
    private List<List<String>> read(Sheet sheet) {
        List<List<String>> dataLst = new ArrayList<List<String>>();

        /** 得到Excel的行数 */
        this.totalRows = sheet.getPhysicalNumberOfRows();

        /** 得到Excel的列数 */
        if (this.totalRows >= 1 && sheet.getRow(0) != null) {
            this.totalCells = sheet.getRow(0).getPhysicalNumberOfCells();
        }

        /** 循环Excel的行 */
        for (int r = 0; r < this.totalRows; r++) {
            Row row = sheet.getRow(r);
            if (row == null) {
                continue;
            }
            List<String> rowLst = new ArrayList<String>();

            /** 循环Excel的列 */
            for (int c = 0; c < this.getTotalCells(); c++) {
                Cell cell = row.getCell(c);
                String cellValue = "";
                if (null != cell) {
                    // 以下是判断数据的类型
                    switch (cell.getCellType()) {
                        case HSSFCell.CELL_TYPE_NUMERIC: // 数字
                            cell.setCellType(Cell.CELL_TYPE_STRING);//转为字符串
                            cellValue = cell.getStringCellValue();//数字转为字符串 接收电话号码
                            break;

                        case HSSFCell.CELL_TYPE_STRING: // 字符串
                            cellValue = cell.getStringCellValue();
                            break;

                        case HSSFCell.CELL_TYPE_BOOLEAN: // Boolean
                            cellValue = cell.getBooleanCellValue() + "";
                            break;

                        case HSSFCell.CELL_TYPE_FORMULA: // 公式
                            cellValue = cell.getCellFormula() + "";
                            break;

                        case HSSFCell.CELL_TYPE_BLANK: // 空值
                            cellValue = "";
                            break;

                        case HSSFCell.CELL_TYPE_ERROR: // 故障
                            cellValue = "非法字符";
                            break;

                        default:
                            cellValue = "未知类型";
                            break;
                    }
                }
                rowLst.add(cellValue);
            }

            /** 保存第r行的第c列 */
            dataLst.add(rowLst);
        }
        return dataLst;
    }



}
