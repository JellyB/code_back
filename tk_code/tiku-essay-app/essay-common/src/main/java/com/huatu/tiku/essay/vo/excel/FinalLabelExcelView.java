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
public class FinalLabelExcelView extends ExcelView {
    @Override
    public void setRow(Sheet sheet, Map<String, Object> map) {
        List<String> scoreList = (List<String>) map.get("scoreList");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("");
        if (CollectionUtils.isNotEmpty(scoreList)) {
            for (int i = 0; i < scoreList.size(); i++) {
                header.createCell(i + 1).setCellValue(scoreList.get(i));
            }
        }

        Map members = (Map<String, Integer>) map.get("members");
        Row userRow = sheet.createRow(1);
        for (int i = 0; i < scoreList.size(); i++) {
            String count = members.get(scoreList.get(i)) == null ? "0" : members.get(scoreList.get(i)).toString();
            userRow.createCell(i + 1).setCellValue(count);
        }
    }

    @Override
    protected void setStyle(Workbook workbook) {
    }
}
