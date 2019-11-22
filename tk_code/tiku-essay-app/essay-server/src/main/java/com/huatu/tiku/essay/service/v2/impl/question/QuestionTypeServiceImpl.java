package com.huatu.tiku.essay.service.v2.impl.question;

import com.huatu.tiku.essay.essayEnum.TemplateEnum;
import com.huatu.tiku.essay.service.v2.question.QuestionTypeService;
import org.springframework.stereotype.Service;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/7/11
 * @描述
 */
@Service
public class QuestionTypeServiceImpl  implements QuestionTypeService {

    /**
     * 将具体的题型转化为批注试题类型
     *
     * @param questionType 为0时代表套题
     * @return
     */
    public int convertQuestionTypeToQuestionLabelType(int questionType) {
    	 if (questionType == 0) {
             return TemplateEnum.QuestionLabelEnum.TT.getCode();
         }else if (questionType == 5) {
            return TemplateEnum.QuestionLabelEnum.YLW.getCode();
        } else if (questionType == 4) {
            return TemplateEnum.QuestionLabelEnum.YYW.getCode();
        } else {
            return TemplateEnum.QuestionLabelEnum.XT.getCode();
        }
    }
}
