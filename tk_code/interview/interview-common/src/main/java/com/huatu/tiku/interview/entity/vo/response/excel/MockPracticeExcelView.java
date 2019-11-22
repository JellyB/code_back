package com.huatu.tiku.interview.entity.vo.response.excel;

import com.huatu.tiku.interview.entity.vo.response.MockPracticeExcelVO;
import com.huatu.tiku.interview.entity.vo.response.ModulePracticeExcelVO;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;
import java.util.Map;

/**
 * @Author: 模块练习数据导出
 * @Date: Created in 2018/1/8 21:54
 * @Modefied By:
 */
public class MockPracticeExcelView extends ExcelView  {
    @Override
    public void setRow(Sheet sheet, Map<String, Object> map) {

        // create header row
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("学员姓名");
        header.createCell(1).setCellValue("地区");
        header.createCell(2).setCellValue("班级");
        header.createCell(3).setCellValue("第一题答案");
        header.createCell(4).setCellValue("第二题答案");
        header.createCell(5).setCellValue("第三题答案");
        header.createCell(6).setCellValue("第四题答案");
        header.createCell(7).setCellValue("第五题答案");
        header.createCell(8).setCellValue("其他");
        header.createCell(9).setCellValue("综合评价");

        List<MockPracticeExcelVO> list = (List<MockPracticeExcelVO>) map.get("members");
        int rowCount = 1;
        for (MockPracticeExcelVO vo : list) {
            Row userSignRow = sheet.createRow(rowCount++);
            userSignRow.createCell(0).setCellValue(vo.getUserName());
            userSignRow.createCell(1).setCellValue(vo.getAreaName());
            userSignRow.createCell(2).setCellValue(vo.getClassName());
            userSignRow.createCell(3).setCellValue(vo.getAnswerList().get(0));
            userSignRow.createCell(4).setCellValue(vo.getAnswerList().get(1));
            userSignRow.createCell(5).setCellValue(vo.getAnswerList().get(2));
            userSignRow.createCell(6).setCellValue(vo.getAnswerList().get(3));
            userSignRow.createCell(7).setCellValue(vo.getAnswerList().get(4));
            userSignRow.createCell(8).setCellValue(vo.getElseRemark());
            userSignRow.createCell(9).setCellValue(vo.getOverAllScore());


        }
    }

    @Override
    protected void setStyle(Workbook workbook) {
//        DefaultCellStyle defaultCellStyle = new DefaultCellStyleImpl();
//        super.cellStyle = defaultCellStyle.setCellStyle(workbook);
    }
}
