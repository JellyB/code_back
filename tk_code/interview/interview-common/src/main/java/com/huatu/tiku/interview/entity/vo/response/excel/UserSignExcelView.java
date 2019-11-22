package com.huatu.tiku.interview.entity.vo.response.excel;

import com.huatu.tiku.interview.entity.vo.response.SignInfoVO;
import com.huatu.tiku.interview.entity.vo.response.SignTimeVO;
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
public class UserSignExcelView extends ExcelView  {
    @Override
    public void setRow(Sheet sheet, Map<String, Object> map) {

        // create header row
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("id");
        header.createCell(1).setCellValue("地区");
        header.createCell(2).setCellValue("班级");
        header.createCell(3).setCellValue("姓名");
        header.createCell(4).setCellValue("打卡信息");

        List<SignInfoVO> list = (List<SignInfoVO>) map.get("members");
        int rowCount = 1;
        for (SignInfoVO userSign : list) {
            Row userSignRow = sheet.createRow(rowCount++);
            userSignRow.createCell(0).setCellValue(userSign.getId());
            userSignRow.createCell(1).setCellValue(userSign.getAreaName());
            userSignRow.createCell(2).setCellValue(userSign.getClassName());
            userSignRow.createCell(3).setCellValue(userSign.getUname());
            List<SignTimeVO> dateList = userSign.getDateList();
            int i = 0;
            for(SignTimeVO timeVO:dateList){
                String date = timeVO.getDate();
                List<String> signList = timeVO.getSignList();
                StringBuilder signListStr = new StringBuilder();
                userSignRow.createCell(4+i).setCellValue(date+":"+signList);
                i++;
            }

        }
    }

    @Override
    protected void setStyle(Workbook workbook) {
//        DefaultCellStyle defaultCellStyle = new DefaultCellStyleImpl();
//        super.cellStyle = defaultCellStyle.setCellStyle(workbook);
    }
}
