package com.huatu.tiku.essay.vo.excel;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;
import java.util.Map;

/**
 * @Author: ZhenYang
 * @Date: Created in 2018/1/8 15:47
 * @Modefied By:
 */
public class UserInfoScoreByAreaExcelView extends ExcelView {
    @Override
    public void setRow(Sheet sheet, Map<String, Object> map) {

        // create header row
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("昵称");
//        header.getCell(0).setCellStyle(super.cellStyle);
        header.createCell(1).setCellValue("手机号");
//        header.getCell(1).setCellStyle(super.cellStyle);
        header.createCell(2).setCellValue("地区");
//        header.getCell(2).setCellStyle(super.cellStyle);
        header.createCell(3).setCellValue("第一题/分");
//        header.getCell(3).setCellStyle(super.cellStyle);
        header.createCell(4).setCellValue("第二题/分");
        header.createCell(5).setCellValue("第三题/分");
        header.createCell(6).setCellValue("第四题/分");
        header.createCell(7).setCellValue("第五题/分");
        header.createCell(8).setCellValue("总分");
//        header.getCell(4).setCellStyle(super.cellStyle);

        @SuppressWarnings("unchecked")
        List<ExcelUserInfoScoreByAreaVO> list = (List<ExcelUserInfoScoreByAreaVO>) map.get("members");
        int rowCount = 1;
        for (ExcelUserInfoScoreByAreaVO user : list) {
            Row userRow = sheet.createRow(rowCount++);
            userRow.createCell(0).setCellValue(user.getUserId());
            userRow.createCell(1).setCellValue(user.getMobile()+"");
            userRow.createCell(2).setCellValue(user.getAreaName());
            userRow.createCell(3).setCellValue(user.getFirstScore());
            userRow.createCell(4).setCellValue(user.getSecondScore());
            userRow.createCell(5).setCellValue(user.getThirdScore());
            userRow.createCell(6).setCellValue(user.getForthScore());
            userRow.createCell(7).setCellValue(user.getFifthScore());
            userRow.createCell(8).setCellValue(user.getScore());

        }
    }

    @Override
    protected void setStyle(Workbook workbook) {
//        DefaultCellStyle defaultCellStyle = new DefaultCellStyleImpl();
//        super.cellStyle = defaultCellStyle.setCellStyle(workbook);
    }
}
