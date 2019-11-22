package com.huatu.tiku.dto;


import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;
import java.util.Map;


/**
 * @创建人 lizhenjuan
 * @创建时间 2019/5/6
 * @描述
 */
public class PaperResultView extends ExcelView {


    @Override
    public void setRow(Sheet sheet, Map<String, Object> map) {

        // create header row
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("学员id");
        header.createCell(1).setCellValue("手机号");
        header.createCell(2).setCellValue("昵称");
        header.createCell(3).setCellValue("考试成绩");
        header.createCell(4).setCellValue("交卷时间");
        header.createCell(5).setCellValue("用户名");
        header.createCell(6).setCellValue("地区id");
        header.createCell(7).setCellValue("地区名称");


        List<MatchResultVO> list = (List<MatchResultVO>) map.get("members");
        int rowCount = 1;
        for (MatchResultVO vo : list) {
            Row userSignRow = sheet.createRow(rowCount++);
            userSignRow.createCell(0).setCellValue(vo.getUserId());
            userSignRow.createCell(1).setCellValue(vo.getMobile());
            userSignRow.createCell(2).setCellValue(vo.getNick());
            userSignRow.createCell(3).setCellValue(vo.getScore());
            userSignRow.createCell(4).setCellValue(vo.getEndTime());
            userSignRow.createCell(5).setCellValue(vo.getName());
            userSignRow.createCell(6).setCellValue(vo.getPositionId());
            userSignRow.createCell(7).setCellValue(vo.getPositionName());

        }
    }

    @Override
    protected void setStyle(Workbook workbook) {
    }
}
