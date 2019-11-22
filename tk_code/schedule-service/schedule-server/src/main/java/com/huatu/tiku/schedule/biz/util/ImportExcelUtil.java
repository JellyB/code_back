package com.huatu.tiku.schedule.biz.util;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by duanxiangchao on 2018/5/11
 */
public class ImportExcelUtil {

    private int totalRows = 0;

    private int totalCells = 0;

    private String errorInfo;

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getTotalCells() {
        return totalCells;
    }

    public void setTotalCells(int totalCells) {
        this.totalCells = totalCells;
    }

    public String getErrorInfo() {
        return errorInfo;
    }

    public void setErrorInfo(String errorInfo) {
        this.errorInfo = errorInfo;
    }

    public boolean validateExcel(String filePath) {
        if (filePath == null || !(ExcelUtil.isExcel2003(filePath) || ExcelUtil.isExcel2007(filePath))) {
            errorInfo = "文件名不是excel格式";
            return false;
        }
        /** 检查文件是否存在 */
        File file = new File(filePath);
        if (file == null || !file.exists()) {
            errorInfo = "文件不存在";
            return false;
        }
        return true;
    }

    /**
     * @description: 根据路径读取excel文件
     * @author duanxiangchao
     * @date 2018/5/16 下午3:29
     */
    public List<List<List<String>>> read(String filePath) {
        List<List<List<String>>> dataLst = new ArrayList<List<List<String>>>();
        InputStream is = null;
        try {
            /** 验证文件是否合法 */
            if (!validateExcel(filePath)) {
                System.out.println(errorInfo);
                return null;
            }

            /** 判断文件的类型，是2003还是2007 */
            boolean isExcel2003 = true;
            if (ExcelUtil.isExcel2007(filePath)) {
                isExcel2003 = false;
            }

            /** 调用本类提供的根据流读取的方法 */
            File file = new File(filePath);
            is = new FileInputStream(file);
            dataLst = read(is, isExcel2003);
            is.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    is = null;
                    e.printStackTrace();
                }
            }
        }
        /** 返回最后读取的结果 */
        return dataLst;
    }

    /**
     * 根据流读取Excel文件
     * @param inputStream
     * @param isExcel2003
     * @return
     */
    public List<List<List<String>>> read(InputStream inputStream, boolean isExcel2003) {
        List<List<List<String>>> dataLst = new ArrayList<List<List<String>>>();;
        try {
            /** 根据版本选择创建Workbook的方式 */
            Workbook wb = null;
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
                            cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                            cellValue = cell.getStringCellValue() + "";
                            break;

                        case HSSFCell.CELL_TYPE_STRING: // 字符串
                            cellValue = cell.getStringCellValue();
                            break;

                        case HSSFCell.CELL_TYPE_BOOLEAN: // Boolean
                            cellValue = cell.getBooleanCellValue() + "";
                            break;

                        case HSSFCell.CELL_TYPE_FORMULA: // 公式
                            cellValue = String.valueOf(cell.getNumericCellValue());
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

    /**
     * 创建一个Excel
     *
     * @param fileName 文件名
     * @param dataList 数据
     * @throws IOException
     */
    public static void buildXLSX(String fileName, List<String[]> dataList) {
        try {
            // 声明一个工作薄
            XSSFWorkbook workBook = null;
            workBook = new XSSFWorkbook();
            // 生成一个表格
            XSSFSheet sheet = workBook.createSheet();
            workBook.setSheetName(0, "info");

            //插入需导出的数据
            for (int i = 0; i < dataList.size(); i++) {
                XSSFRow row = sheet.createRow(i);
                String[] oneRowData = dataList.get(i);
                for (int j = 0; j < oneRowData.length; j++) {
                    row.createCell(j).setCellValue(oneRowData[j]);
                }
            }
            File file = new File("输出\\" + fileName);
            //文件输出流
            FileOutputStream outStream = new FileOutputStream(file);
            workBook.write(outStream);
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @描述：main测试方法
     * @作者：建宁
     * @时间：2012-08-29 下午17:12:15
     * @参数：@param args
     * @参数：@throws Exception
     * @返回值：void
     */
    public static void main(String[] args) throws Exception {
        ImportExcelUtil poi = new ImportExcelUtil();
        List<List<List<String>>> list = poi.read("/Users/laobo/excel/shupai.xlsx");
        if (list != null) {
            for(int z = 0; z < list.size(); z ++){
                System.out.println("========================sheet" + (z + 1) + "=====================");
                List<List<String>> sheetList = list.get(z);
                for (int i = 0; i < list.size(); i++) {
                    System.out.print("第" + (i) + "行");
                    List<String> cellList = sheetList.get(i);
                    for (int j = 0; j < cellList.size(); j++) {
                        // System.out.print("    第" + (j + 1) + "列值：");
                        System.out.print("    " + cellList.get(j));
                    }
                    System.out.println();
                }

            }

        }




    }

}
