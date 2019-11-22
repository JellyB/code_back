package com.huatu.tiku.essay.web.controller.api.V1.report;

import com.huatu.common.exception.BizException;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.service.v2.question.QuestionReportService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.vo.resp.correct.report.EssayQuestionCorrectReportVO;
import com.huatu.tiku.springboot.users.support.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/7/11
 * @描述 人工批改,单题报告
 */
@RestController
@Slf4j
@RequestMapping(value = "api/v1/question/report", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class ApiQuestionReportController {


    @Autowired
    QuestionReportService questionReportService;


    /**
     * 查询批改报告
     *
     * @param userSession
     * @return
     * @throws BizException
     */
    @LogPrint
    @RequestMapping(value = "/{answerId}", method = RequestMethod.GET)
    public EssayQuestionCorrectReportVO getPaperReport(@Token UserSession userSession,
                                                       @PathVariable long answerId) throws BizException {
        return questionReportService.getQuestionReport(answerId);
    }

}
