package com.huatu.tiku.match.web.controller.v1.paper;

import com.huatu.springboot.web.version.mapping.annotation.ApiVersion;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.match.bo.paper.AnswerCardSimpleBo;
import com.huatu.tiku.match.dto.paper.AnswerDTO;
import com.huatu.tiku.match.service.v1.paper.AnswerCardService;
import com.huatu.tiku.match.service.v1.reward.PaperRewardService;
import com.huatu.tiku.springboot.users.support.Token;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.SuccessMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 答卷
 * Created by lijun on 2018/10/18
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/answerCard")
@ApiVersion("v1")
public class AnswerCardController {

    final AnswerCardService answerCardService;

    final PaperRewardService paperRewardService;
    /**
     * 创建答题卡
     */
    @LogPrint
    @PutMapping(value = "{paperId}")
    public Object createAnswerCard(@Token UserSession userSession, @RequestHeader int terminal, @PathVariable int paperId) throws BizException {
        AnswerCardSimpleBo answerCardSimpleBo = answerCardService.createAnswerCard(userSession, paperId, terminal);
        return answerCardSimpleBo;
    }

    /**
     * 用户答题数据保存
     */
    @LogPrint
    @PostMapping(value = "{practiceId}/save")
    public Object save(@Token UserSession userSession, @PathVariable long practiceId, @RequestBody List<AnswerDTO> answerList) {
        return answerCardService.save(userSession.getId(), practiceId, answerList);
    }

    /**
     * 用户答题卡保存 -- 交卷
     */
    @LogPrint
    @PostMapping(value = "{practiceId}/submit")
    public Object submit(@Token UserSession userSession, @PathVariable long practiceId, @RequestBody List<AnswerDTO> answerList) {
        answerCardService.submit2Queue(userSession.getId(), practiceId, answerList);
        //积分添加
        paperRewardService.sendMatchSubmitMsg(userSession.getId(),userSession.getUname(),practiceId);
        return SuccessMessage.create("操作成功");
    }

    /**
     * 获取错题解析试题详情
     */
    @LogPrint
    @GetMapping(value = "{practiceId}/getWrongQuestionAnalysis")
    public Object getWrongQuestionAnalysis(@PathVariable long practiceId) {
        return answerCardService.getWrongQuestionAnalysis(practiceId);
    }

    /**
     * 获取答题时试题详情
     */
    @LogPrint
    @GetMapping(value = "{practiceId}/getAllAnalysisInfo")
    public Object questionAnalysisInfo(@PathVariable long practiceId) {
        return answerCardService.getAllQuestionAnalysis(practiceId);
    }

}
