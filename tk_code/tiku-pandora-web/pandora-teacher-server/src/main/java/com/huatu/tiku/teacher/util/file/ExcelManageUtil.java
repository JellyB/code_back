package com.huatu.tiku.teacher.util.file;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;


/**
 * 从excel读取数据/往excel中写入 excel有表头，表头每列的内容对应实体类的属性
 */
public class ExcelManageUtil {
    private final static Logger logger = LoggerFactory.getLogger(ExcelManageUtil.class);
    private final static int TOTAL_LINE = 60000;

    public static void writer(String path, String fileName, String fileType, List<List> list, String titleRow[]) throws IOException {
        Workbook wb = null;
        String excelPath = path+File.separator+fileName+"."+fileType;
        File file = new File(excelPath);
        if(file.getParentFile().exists()){
            new File(file.getParent()).mkdir();
        }
        Sheet sheet =null;
        //创建工作文档对象
        if (!file.exists()) {
            if (fileType.equals("xls")) {
                wb = new HSSFWorkbook();

            } else if(fileType.equals("xlsx")) {
                wb = new XSSFWorkbook();
            } else {
                logger.error("文件格式不正确");
            }
        } else {
            if (fileType.equals("xls")) {
                wb = new HSSFWorkbook();

            } else if(fileType.equals("xlsx")) {
                wb = new XSSFWorkbook();

            } else {
                logger.error("文件格式不正确");
            }
        }
        //创建sheet对象
        int total = list.size();
        int index = 0;
        int suffix = 1;
        while(true){
            int end = index+TOTAL_LINE>total?total:index+ TOTAL_LINE;
            sheet = (Sheet) wb.createSheet("sheet"+suffix);
            setExcelCell(wb,sheet,list.subList(index,end),titleRow);
            if(end==total){
                break;
            }
            suffix ++;
            index = end;
        }


        //创建文件流
        OutputStream stream = new FileOutputStream(excelPath);
        //写入数据
        wb.write(stream);
        //关闭文件流
        stream.close();
    }

    private static void setExcelCell(Workbook wb, Sheet sheet, List<List> list, String[] titleRow) {
        CellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(HSSFColor.PALE_BLUE.index);
        Row row = sheet.createRow(0);    //创建第二行
        Cell cell ;
        for(int i = 0;i < titleRow.length;i++){
            cell = row.createCell(i);
            cell.setCellValue(titleRow[i]);
            cell.setCellStyle(style); // 样式，居中
            sheet.setColumnWidth(i, 20 * 256);
        }
        row.setHeight((short) 250);

        //循环写入行数据
        int line = 1;
        for (List temp:list) {
            row = (Row) sheet.createRow(line);
            row.setHeight((short) 250);
            if(CollectionUtils.isNotEmpty(temp)){
                for(int i = 0 ;i<temp.size();i++){
                    row.createCell(i).setCellValue(String.valueOf(temp.get(i)));
                }
            }else{
                row.createCell(0).setCellValue("");
            }
            line++;
        }
    }

    public static List<List> readExcel(String path) throws Exception {
        List<List> list = Lists.newArrayList();
        HSSFWorkbook workbook = new HSSFWorkbook(new FileInputStream(new File(path)));
        HSSFSheet sheet = null;
        // 获取每个Sheet表
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            sheet = workbook.getSheetAt(i);
            // getLastRowNum，获取最后一行的行标
            for (int j = 0; j < sheet.getLastRowNum() + 1; j++) {
                HSSFRow row = sheet.getRow(j);
                List rowList = Lists.newArrayList();
                if (row != null) {
                    // getLastCellNum，是获取最后一个不为空的列是第几个
                    for (int k = 0; k < row.getLastCellNum(); k++) {
                        // getCell 获取单元格数据
                        if (row.getCell(k) != null) {
                            rowList.add(row.getCell(k));
                        }
                    }
                }
                list.add(rowList);
            }
            System.out.println("读取sheet表：" + workbook.getSheetName(i) + " 完成");
        }
        return list;
    }

}