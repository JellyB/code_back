package com.huatu.tiku.essay.web.controller.api.V2;

import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.vo.resp.EssayPaperQuestionVO;
import com.huatu.tiku.essay.service.EssayMaterialService;
import com.huatu.tiku.essay.service.EssayPaperService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.util.admin.EssayAnalyzeUtil;
import com.huatu.tiku.springboot.users.support.Token;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * 套题
 * Created by huangqp on 2017\11\23 0023.
 */
@RestController
@RequestMapping("api/v2/paper")
@Slf4j
public class ApiEssayPaperControllerV2 {

    @Autowired
    EssayPaperService essayPaperService;
    @Autowired
    EssayMaterialService essayMaterialService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;


    /**
     * 获取试卷题目
     *
     * @param userSession
     * @param terminal
     * @param cv
     * @param paperId
     * @return
     */
    @LogPrint
    @GetMapping(value = "questionList/{paperId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object getQuestionsWithAnswer(@Token UserSession userSession,
                                         @RequestHeader int terminal,
                                         @RequestHeader String cv,
                                         @PathVariable(name = "paperId") long paperId,
                                         @RequestParam(defaultValue = "1") int modeType) {
        int userId = userSession.getId();
        EssayPaperQuestionVO essayPaperQuestionVO = essayPaperService.findQuestionDetailByPaperIdV2(paperId, userId, EssayAnswerCardEnum.ModeTypeEnum.create(modeType));
        essayPaperQuestionVO.getEssayQuestions().forEach(question -> {
            if (StringUtils.isNoneBlank(question.getAnswerTask())
                    && StringUtils.isNoneBlank(question.getAnswerRange())
                    && StringUtils.isNoneBlank(question.getAnswerDetails())) {
                /**
                 * @create huang 2018-4-21
                 * @from 试卷分析内容使用任务，细节，范围三个字段拼接
                 */
                question.setAnalyzeQuestion(EssayAnalyzeUtil.assertAnalyze(question.getAnswerTask(), question.getAnswerRange(), question.getAnswerDetails()));
            }
        });
        return essayPaperQuestionVO;
    }
}
