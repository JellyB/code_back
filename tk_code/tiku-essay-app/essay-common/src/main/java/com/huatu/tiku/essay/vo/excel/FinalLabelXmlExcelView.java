package com.huatu.tiku.essay.vo.excel;

import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;
import java.util.Map;

/**
 * @Author: zhaoxi
 * @Date: Created in 2018/1/8 21:54
 * @Modefied By:终审批注数据导出excelVO
 */
public class FinalLabelXmlExcelView extends ExcelView  {
    @Override
    public void setRow(Sheet sheet, Map<String, Object> map) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("批注id");
        header.createCell(1).setCellValue("批注xml内容");

        List<FinalLabelXmlExcelVO> list = (List<FinalLabelXmlExcelVO>) map.get("members");
        int rowCount = 1;
        for (FinalLabelXmlExcelVO vo : list) {
            Row userRow = sheet.createRow(rowCount++);
            userRow.createCell(0).setCellValue(vo.getId());
            userRow.createCell(1).setCellValue(vo.getContent());


        }
    }

    @Override
    protected void setStyle(Workbook workbook) {
    }
}
