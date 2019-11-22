package com.huatu.tiku.match.web.controller.v1.search;

import com.huatu.springboot.web.version.mapping.annotation.ApiVersion;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.match.service.impl.v1.search.SearchHandler;
import com.huatu.tiku.springboot.users.service.UserSessionService;
import com.huatu.tiku.springboot.users.support.Token;
import com.huatu.ztk.commons.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;


/**
 * 描述： 1.模考大赛 搜索接口
 *
 * @author biguodong
 * Create time 2018-10-16 下午1:28
 **/
@RestController
@RequestMapping(value = "search", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@ApiVersion("v1")
@Slf4j
public class SearchControllerV1 {

    @Autowired
    private SearchHandler searchHandler;
    @Autowired
    private UserSessionService userSessionService;

    @GetMapping
    public Object matches(@Token(required = false, check = false) UserSession userSession,
                          @RequestHeader(value = "subject", defaultValue = "-1") int subject,
                          @RequestParam(defaultValue = "-1") int subjectId,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "100") int size) throws BizException {
        if (subjectId > 0) {
            subject = subjectId;
        }
        if (null != userSession) {
            userSessionService.assertSession(userSession);
        }
        return searchHandler.dealSearch(userSession, subject, page, size,
                SearchHandler.DEFAULT_HEAD_FILTER);
    }
}
