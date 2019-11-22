package com.huatu.tiku.essay.vo.excel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;
import java.util.Map;

/**
 * @Author: zhaoxi
 * @Date: Created in 2018/1/8 21:54
 * @Modefied By:教师批注工作量统计excelVO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeacherLabelExcelVO{
    //日期
    private String  teacherName;
    //每日批注数量
    private List<Integer> countList;
}
