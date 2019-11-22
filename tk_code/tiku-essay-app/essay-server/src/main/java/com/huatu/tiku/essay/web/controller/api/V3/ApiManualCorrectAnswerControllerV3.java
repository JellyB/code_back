package com.huatu.tiku.essay.web.controller.api.V3;

import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.service.UserAnswerService;
import com.huatu.tiku.essay.service.v2.question.QuestionCorrectDetailService;
import com.huatu.tiku.essay.vo.resp.EssayQuestionVO;
import com.huatu.tiku.springboot.users.support.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/7/9
 * @描述 批改详情
 */

@RestController
@Slf4j
@RequestMapping("api/v3/answer")
public class ApiManualCorrectAnswerControllerV3 {

    @Autowired
    UserAnswerService userAnswerService;

    @Autowired
    QuestionCorrectDetailService questionCorrectDetailService;


    /**
     * @param userSession
     * @param type
     * @param answerId
     * @param correctMode 批改模式,1智能批改,2人工批改,3智能转人工
     * @param terminal
     * @param cv
     * @return
     */
    @RequestMapping("correctDetail/{type}/{answerId}")
    public Object correctDetail(@Token UserSession userSession,
                                @PathVariable int type,
                                @PathVariable int answerId,
                                @RequestParam(defaultValue = "1") int correctMode,
                                @RequestHeader int terminal,
                                @RequestHeader String cv) {

        List<EssayQuestionVO> essayQuestionVOS = questionCorrectDetailService.answerDetailV3(userSession.getId(), type, answerId, terminal, cv);
        //判断客户端版本,处理批改详情中的新标签（安卓6.3之后的版本支持新标签，ios7.0）
        for (EssayQuestionVO vo : essayQuestionVOS) {
            vo.setCorrectRule(null);
            // 如果没有批改规则 除应用文和议论文之外都是关键句
            if (vo.getCorrectType() == 0) {
                if (vo.getType() == 4 || vo.getType() == 5) {
                    vo.setCorrectType(2);
                } else {
                    vo.setCorrectType(1);
                }
            }
        }
        return essayQuestionVOS;

    }


}
