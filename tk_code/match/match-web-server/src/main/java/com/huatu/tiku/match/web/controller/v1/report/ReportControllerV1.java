package com.huatu.tiku.match.web.controller.v1.report;

import com.huatu.springboot.web.version.mapping.annotation.ApiVersion;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.match.service.v1.report.ReportService;
import com.huatu.tiku.springboot.users.support.Token;
import com.huatu.ztk.commons.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2018-12-27 下午5:23
 **/

@Slf4j
@RestController
@RequestMapping(value = "report")
@ApiVersion(value = "v1")
public class ReportControllerV1 {


    @Autowired
    private ReportService reportService;

    /**
     * 获取当前tag 下我的模考报告
     *
     * @param userSession
     * @param tagId
     * @return
     * @throws BizException
     */
    @LogPrint
    @GetMapping(value = "{tagId}")
    public Object list(@Token UserSession userSession,
                       @PathVariable(value = "tagId") int tagId,
                       @RequestParam(defaultValue = "-1") int subjectId,
                       @RequestHeader(defaultValue = "-1") int subject,
                       @RequestHeader(defaultValue = "7.1.140") String cv,
                       @RequestHeader(defaultValue = "1") int terminal) throws BizException {
        int userId = userSession.getId();
        if (subjectId > 0) {
            subject = subjectId;
        }

        if (subject < 0) {
            if(terminal == 21){
                subject  = 1;
            }else{
                subject = userSession.getSubject();
            }
        }
        return reportService.myReportList(userId, tagId, subject, cv, terminal);
    }
}
