package com.huatu.tiku.essay.vo.excel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author zhaoxi
 * @Description: 批注内容导出VO
 * @date 2018/10/17下午3:15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FinalLabelXmlExcelVO {
    //批注id
    private String  id;
    //xml内容
    private String content;
}
