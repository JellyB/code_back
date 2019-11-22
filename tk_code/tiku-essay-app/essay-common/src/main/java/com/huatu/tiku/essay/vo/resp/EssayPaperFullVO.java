package com.huatu.tiku.essay.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.huatu.tiku.essay.vo.admin.EssayMockExamVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by huangqp on 2018\2\26 0026.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EssayPaperFullVO {
    private EssayMockExamVO essayMockExamVO;
    private List<EssayMaterialVO> materials;
    private List<EssayQuestionFullVO> questionFullVOS;
}
