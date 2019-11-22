package com.huatu.tiku.essay.vo.export;

import com.huatu.tiku.essay.entity.EssayStandardAnswer;
import com.huatu.tiku.essay.vo.admin.AdminExportKeyWordAndPhraseVO;
import com.huatu.tiku.essay.vo.admin.AdminQuestionFormatVO;
import com.huatu.tiku.essay.vo.admin.AdminQuestionKeyRuleVO;
import com.huatu.tiku.essay.vo.resp.EssayQuestionDeductRuleVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author zhaoxi
 * @Description: 后台导出试题相关VO
 * @date 2018/10/8下午1:25
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EssayExportQuestionVO {
    /**
     * 序号
     */
    private int sort;
    /**
     * 题干
     */
    private String stem;
    /**
     * 答案
     */
    private List<EssayStandardAnswer> answerList;
    /**
     * 解析
     */
    private String analysis;
    /**
     * 经验小结
     */
    private String review;
    /**
     * 解析
     */
    private double difficultGrade;
    /**
     * 算法
     */
    private AdminExportKeyWordAndPhraseVO adminExportKeyWordAndPhraseVO;

    /**
     * correctType 算法类型 (1 关键词 2关键句)
     */
    private int correctType;

    /**
     * 试题类型
     */
    private Integer questionType;

    private AdminQuestionKeyRuleVO questionKeyRuleVO;

    /**
     * 文章格式
     */
    private AdminQuestionFormatVO questionFormatVO;

    /**
     * 扣分项
     */
    private EssayQuestionDeductRuleVO deductRuleVO;


}
