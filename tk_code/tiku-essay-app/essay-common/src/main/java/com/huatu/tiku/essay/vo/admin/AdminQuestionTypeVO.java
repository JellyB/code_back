package com.huatu.tiku.essay.vo.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Created by x6 on 2018/5/24.
 */
@Builder
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminQuestionTypeVO {
    private String questionTypeName;

    private List<Long> questionType;
}
