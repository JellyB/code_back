package com.huatu.tiku.essay.vo.excel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author zhaoxi
 * @Description: 批注终审分数分布对象
 * @date 2018/10/17下午3:15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FinalLabelExcelVO {
    //日期
    private String  date;
    //各分数段人数
    private List<Integer> countList;
}
