package com.huatu.tiku.essay.vo.resp;

import com.huatu.tiku.essay.vo.admin.AdminPaperWithQuestionVO;
import com.huatu.tiku.essay.vo.admin.AdminQuestionFullVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by huangqp on 2017\12\6 0006.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class EssayPaperDetailVO {
    private AdminPaperWithQuestionVO essayPaper;
    private List<EssayMaterialVO> essayMaterials;
    private AdminQuestionFullVO question;
}
