package com.huatu.tiku.essay.vo.excel;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;
import java.util.Map;

/**
 * @Author: ZhenYang
 * @Date: Created in 2018/1/8 21:54
 * @Modefied By:
 */
public class UserQuestionExcelView extends ExcelView  {
    @Override
    public void setRow(Sheet sheet, Map<String, Object> map) {

        // create header row
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("昵称");
//        header.getCell(0).setCellStyle(super.cellStyle);
        header.createCell(1).setCellValue("手机号");
//        header.getCell(1).setCellStyle(super.cellStyle);
        header.createCell(2).setCellValue("成绩");
//        header.getCell(4).setCellStyle(super.cellStyle);

        @SuppressWarnings("unchecked")
        List<ExcelQuestionUserInfoVO> list = (List<ExcelQuestionUserInfoVO>) map.get("members");
        int rowCount = 1;
        for (ExcelQuestionUserInfoVO user : list) {
            Row userRow = sheet.createRow(rowCount++);
            userRow.createCell(0).setCellValue(user.getUserId());
            userRow.createCell(1).setCellValue(user.getMobile()+"");
            userRow.createCell(2).setCellValue(user.getScores().toString());

        }
    }

    @Override
    protected void setStyle(Workbook workbook) {
//        DefaultCellStyle defaultCellStyle = new DefaultCellStyleImpl();
//        super.cellStyle = defaultCellStyle.setCellStyle(workbook);
    }
}
