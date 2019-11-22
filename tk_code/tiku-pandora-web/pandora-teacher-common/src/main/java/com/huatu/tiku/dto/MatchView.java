package com.huatu.tiku.dto;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;
import java.util.Map;

/**
 * @Author: zhaoxi
 */
public class MatchView extends ExcelView {
    @Override
    public void setRow(Sheet sheet, Map<String, Object> map) {

        // create header row
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("模考id");
        header.createCell(1).setCellValue("模考名称");
        header.createCell(2).setCellValue("模考时间信息");
        header.createCell(3).setCellValue("模考标签");
        header.createCell(4).setCellValue("报名人数");
        header.createCell(5).setCellValue("考试人数");

        List<MatchDataVO> list = (List<MatchDataVO>) map.get("members");
        int rowCount = 1;
        for (MatchDataVO mock : list) {
           if(mock.getEnrollCount() > 0){
               Row userSignRow = sheet.createRow(rowCount++);
               userSignRow.createCell(0).setCellValue(mock.getId());
               userSignRow.createCell(1).setCellValue(mock.getName());
               userSignRow.createCell(2).setCellValue(mock.getTimeInfo());
               userSignRow.createCell(3).setCellValue(mock.getTag());
               userSignRow.createCell(4).setCellValue(mock.getEnrollCount());
               userSignRow.createCell(5).setCellValue(mock.getExamCount());
           }

        }
    }

    @Override
    protected void setStyle(Workbook workbook) {

    }
}
