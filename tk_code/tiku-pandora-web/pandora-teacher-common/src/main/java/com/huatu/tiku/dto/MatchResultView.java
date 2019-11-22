package com.huatu.tiku.dto;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;
import java.util.Map;

/**
 * @Author: zhaoxi
 */
public class MatchResultView extends ExcelView {
    @Override
    public void setRow(Sheet sheet, Map<String, Object> map) {

        // create header row
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("学员id");
        header.createCell(1).setCellValue("手机号");
        header.createCell(2).setCellValue("昵称");
        header.createCell(3).setCellValue("模考名称");
        header.createCell(4).setCellValue("考试成绩");
        header.createCell(5).setCellValue("交卷时间");
        header.createCell(6).setCellValue("考试用时（单位：分）");
        header.createCell(7).setCellValue("第一模块得分");
        header.createCell(8).setCellValue("第二模块得分）");
        header.createCell(9).setCellValue("第三模块得分");
        header.createCell(10).setCellValue("第四模块得分");
        header.createCell(11).setCellValue("第五模块得分");
        header.createCell(12).setCellValue("第六模块得分");
       /* header.createCell(11).setCellValue("手机号");
        header.createCell(12).setCellValue("昵称");*/


        List<MatchResultVO> list = (List<MatchResultVO>) map.get("members");
        int rowCount = 1;
        for (MatchResultVO vo : list) {
            Row userSignRow = sheet.createRow(rowCount++);
            userSignRow.createCell(0).setCellValue(vo.getUserId());
            userSignRow.createCell(1).setCellValue(vo.getMobile());
            userSignRow.createCell(2).setCellValue(vo.getNick());
            userSignRow.createCell(3).setCellValue(vo.getPaperName());
            userSignRow.createCell(4).setCellValue(vo.getScore());
            userSignRow.createCell(5).setCellValue(vo.getEndTime());
            userSignRow.createCell(6).setCellValue(vo.getExpendTime());
            userSignRow.createCell(7).setCellValue(vo.getModuleScore().get(0));
            userSignRow.createCell(8).setCellValue(vo.getModuleScore().get(1));
            userSignRow.createCell(9).setCellValue(vo.getModuleScore().get(2));
            userSignRow.createCell(10).setCellValue(vo.getModuleScore().get(3));
            userSignRow.createCell(11).setCellValue(vo.getModuleScore().get(4));
            if(vo.getModuleScore().size()> 5 ){
                userSignRow.createCell(12).setCellValue(vo.getModuleScore().get(5));
            }
            /*userSignRow.createCell(11).setCellValue(vo.getMobile());
            userSignRow.createCell(12).setCellValue(vo.getNick());*/

        }
    }

    @Override
    protected void setStyle(Workbook workbook) {
//        DefaultCellStyle defaultCellStyle = new DefaultCellStyleImpl();
//        super.cellStyle = defaultCellStyle.setCellStyle(workbook);
    }
}
