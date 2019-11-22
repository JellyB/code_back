package com.huatu.tiku.interview.entity.vo.response.excel;

import com.huatu.tiku.interview.entity.vo.response.ModulePracticeExcelVO;
import com.huatu.tiku.interview.entity.vo.response.PaperPracticeExcelVO;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;
import java.util.Map;

/**
 * @Author: 真题演练数据导出
 * @Date: Created in 2018/1/8 21:54
 * @Modefied By:
 */
public class PaperPracticeExcelView extends ExcelView  {
    @Override
    public void setRow(Sheet sheet, Map<String, Object> map) {

        // create header row
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("学员姓名");
        header.createCell(1).setCellValue("地区");
        header.createCell(2).setCellValue("班级");
        header.createCell(3).setCellValue("仪态自然程度");
        header.createCell(4).setCellValue("语言流畅程度");
        header.createCell(5).setCellValue("扣题精准程度");
        header.createCell(6).setCellValue("调理清晰程度");
        header.createCell(7).setCellValue("内容丰满程度");
        header.createCell(8).setCellValue("其他评价");

        List<PaperPracticeExcelVO> list = (List<PaperPracticeExcelVO>) map.get("members");
        int rowCount = 1;
        for (PaperPracticeExcelVO vo : list) {
            Row userSignRow = sheet.createRow(rowCount++);
            userSignRow.createCell(0).setCellValue(vo.getUserName());
            userSignRow.createCell(1).setCellValue(vo.getAreaName());
            userSignRow.createCell(2).setCellValue(vo.getClassName());
            userSignRow.createCell(3).setCellValue(vo.getBehavior());
            userSignRow.createCell(4).setCellValue(vo.getLanguageExpression());
            userSignRow.createCell(5).setCellValue(vo.getFocusTopic());
            userSignRow.createCell(6).setCellValue(vo.getIsOrganized());
            userSignRow.createCell(7).setCellValue(vo.getHaveSubstance());
            userSignRow.createCell(8).setCellValue(vo.getElseRemark());


        }
    }

    @Override
    protected void setStyle(Workbook workbook) {
//        DefaultCellStyle defaultCellStyle = new DefaultCellStyleImpl();
//        super.cellStyle = defaultCellStyle.setCellStyle(workbook);
    }
}
