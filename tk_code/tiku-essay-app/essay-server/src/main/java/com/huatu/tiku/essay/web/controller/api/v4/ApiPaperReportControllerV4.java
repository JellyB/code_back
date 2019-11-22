package com.huatu.tiku.essay.web.controller.api.v4;

import com.huatu.common.exception.BizException;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.service.EssayPaperReportService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.vo.resp.EssayPaperReportVO;
import com.huatu.tiku.springboot.users.support.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * 套题批改报告（人工批改）
 */
@RestController
@Slf4j
@RequestMapping(value = "api/v4/paper/report", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class ApiPaperReportControllerV4 {

    @Autowired
    EssayPaperReportService essayPaperReportService;

    /**
     * 查询批改报告
     *
     * @param userSession
     * @return
     * @throws BizException
     */
    @LogPrint
    @RequestMapping(value = "/{answerId}", method = RequestMethod.GET)
    public EssayPaperReportVO getPaperReport(@Token UserSession userSession,
                                             @PathVariable long answerId) throws BizException {

        return essayPaperReportService.getReport(answerId);
    }


}
