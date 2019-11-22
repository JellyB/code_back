package com.huatu.tiku.essay.vo.excel;

import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 教师工作量
 */
public class TeacherLabelExcelView extends ExcelView {
    @Override
    public void setRow(Sheet sheet, Map<String, Object> map) {

        // create header row
        Row header = sheet.createRow(0);
        List<String> dateList = (List<String>) map.get("dateList");
        header.createCell(0).setCellValue("");
        if (CollectionUtils.isNotEmpty(dateList)) {
            for (int i = 0; i < dateList.size(); i++) {
                header.createCell(i + 1).setCellValue(dateList.get(i));
            }
            header.createCell(dateList.size() + 1).setCellValue("总批改数");

        }
        Map<String, Map<String, Integer>> members = (Map<String, Map<String, Integer>>) map.get("members");
        int rowCount = 1;
        Set<String> teachers = members.keySet();
        for (String teacher : teachers) {
            Row userRow = sheet.createRow(rowCount++);
            Map<String, Integer> dateCount = members.get(teacher);
            userRow.createCell(0).setCellValue(teacher);
            for (int i = 0; i < dateList.size(); i++) {
                Integer count = dateCount.get(dateList.get(i));
                userRow.createCell(i+1).setCellValue(count == null ? 0 : count);
            }
            userRow.createCell(dateList.size()+1).setCellValue(dateCount.get("total") == null ? 0 : dateCount.get("total"));
        }

    }

    @Override
    protected void setStyle(Workbook workbook) {

    }
}
