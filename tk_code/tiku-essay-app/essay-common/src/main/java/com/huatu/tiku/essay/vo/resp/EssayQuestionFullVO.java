package com.huatu.tiku.essay.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.huatu.tiku.essay.entity.EssayQuestionBase;
import com.huatu.tiku.essay.vo.admin.AdminQuestionDeductRuleVO;
import com.huatu.tiku.essay.vo.admin.AdminQuestionFormatVO;
import com.huatu.tiku.essay.vo.admin.AdminQuestionKeyRuleVO;
import com.huatu.tiku.essay.vo.admin.AdminQuestionVO;
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
public class EssayQuestionFullVO {
    private EssayQuestionBase essayQuestionBase;
    private AdminQuestionVO adminQuestionVO;
    private List<EssayMaterialVO> materialList;
    private AdminQuestionKeyRuleVO adminQuestionKeyRuleVO;
    private AdminQuestionDeductRuleVO adminQuestionDeductRuleVO;
    private AdminQuestionFormatVO adminQuestionFormatVO;
}
