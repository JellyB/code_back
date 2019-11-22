package com.huatu.tiku.essay.vo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author create by jbzm on 2018年1月4日17:48:41
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EssaySimilarQuestionGroupVO {
    /**
     * 单题组id
     */
    private Long id;
    /**
     * 单题组题干
     */
    private String showMsg;
    /**
     * 1归纳概括、 2 综合分析、3 提出对策、4 应用文、5 议论文
     */
    private Integer type;
    /**
     * 单题VO类
     */
    private List<EssaySimilarQuestionVO> essaySimilarQuestionVOList;
}
