package com.huatu.tiku.interview.entity.vo.response.excel;

import com.huatu.tiku.interview.entity.vo.response.ModulePracticeExcelVO;
import com.huatu.tiku.interview.entity.vo.response.SignInfoVO;
import com.huatu.tiku.interview.entity.vo.response.SignTimeVO;
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
public class ModulePracticeExcelView extends ExcelView  {
    @Override
    public void setRow(Sheet sheet, Map<String, Object> map) {

        // create header row
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("学员姓名");
        header.createCell(1).setCellValue("地区");
        header.createCell(2).setCellValue("班级");
        header.createCell(3).setCellValue("流畅程度");
        header.createCell(4).setCellValue("语音语调");
        header.createCell(5).setCellValue("仪态动作");
        header.createCell(6).setCellValue("优点");
        header.createCell(7).setCellValue("缺点");
        header.createCell(8).setCellValue("其他");
        header.createCell(9).setCellValue("综合评价");

        List<ModulePracticeExcelVO> list = (List<ModulePracticeExcelVO>) map.get("members");
        int rowCount = 1;
        for (ModulePracticeExcelVO vo : list) {
            Row userSignRow = sheet.createRow(rowCount++);
            userSignRow.createCell(0).setCellValue(vo.getUserName());
            userSignRow.createCell(1).setCellValue(vo.getAreaName());
            userSignRow.createCell(2).setCellValue(vo.getClassName());
            userSignRow.createCell(3).setCellValue(vo.getFluencyDegreeStr());
            userSignRow.createCell(4).setCellValue(vo.getPronunciationStr());
            userSignRow.createCell(5).setCellValue(vo.getDeportmentStr());
            userSignRow.createCell(6).setCellValue(vo.getAdvantageStr());
            userSignRow.createCell(7).setCellValue(vo.getDisAdvantageStr());
            userSignRow.createCell(8).setCellValue(vo.getElseRemark());
            userSignRow.createCell(9).setCellValue(vo.getTotalRemark());


        }
    }

    @Override
    protected void setStyle(Workbook workbook) {
//        DefaultCellStyle defaultCellStyle = new DefaultCellStyleImpl();
//        super.cellStyle = defaultCellStyle.setCellStyle(workbook);
    }
}
