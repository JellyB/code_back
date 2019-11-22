package com.huatu.tiku.response.paper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.huatu.tiku.response.question.v1.SelectQuestionRespV1;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by x6 on 2018/7/31.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SelectModuleResp {
    /**
     * 模块名称
     */
    private String moduleName;
    /**
     * 模块顺序
     */
    private Integer sort;
    /**
     * 模块下题数
     */
    private Integer qcount;
    /**
     * 模块下试题信息
     */
    private List<SelectQuestionRespV1> questions;
}
