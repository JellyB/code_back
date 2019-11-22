package com.huatu.tiku.essay.vo.excel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhaoxi
 * @Description: 学员成绩
 * @date 2018/8/30下午3:16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExcelQuestionUserInfoVO {
    private Integer userId;
    private Object mobile;
    private List<String> scores = new ArrayList<>();

}
