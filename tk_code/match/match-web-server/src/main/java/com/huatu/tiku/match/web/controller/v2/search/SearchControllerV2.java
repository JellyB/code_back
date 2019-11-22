package com.huatu.tiku.match.web.controller.v2.search;

import com.google.common.collect.Lists;
import com.huatu.springboot.web.version.mapping.annotation.ApiVersion;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.match.bo.MatchHeadUserBo;
import com.huatu.tiku.match.service.impl.v1.search.SearchHandler;
import com.huatu.tiku.match.service.v1.search.MatchStatusService;
import com.huatu.tiku.springboot.users.support.Token;
import com.huatu.ztk.commons.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 描述： 1.模考大赛 首页相关接口
 *
 * @author huangqingpeng
 **/
@RestController
@RequestMapping(value = "search", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@ApiVersion("v2")
@Slf4j
public class SearchControllerV2 {

    @Autowired
    private SearchHandler searchHandler;

    @Autowired
    private MatchStatusService matchStatusService;

    @LogPrint
    @GetMapping("list")
    public Object matches(@RequestParam(defaultValue = "-1") int subjectId,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "100") int size) throws BizException {
        if (subjectId < 0) {
            return Lists.newArrayList();
        }
        //游客模式返回字段
        return searchHandler.dealSearch(null, subjectId, page, size,
                SearchHandler.DEFAULT_HEAD_FILTER);
    }

    @LogPrint
    @GetMapping("list/user")
    public Object matchHeaderUserInfo(@Token UserSession userSession,
                                      @RequestHeader(defaultValue = "-1") int terminal,
                                      @RequestHeader(defaultValue = "1") String cv,
                                      @RequestHeader(defaultValue = "-1") int subject,
                                      @RequestParam(defaultValue = "-1") int subjectId,
                                      @RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "100") int size) throws BizException {
        if(subjectId > 0){
           subject = subjectId;
        }
        List<MatchHeadUserBo> matchHeadUserInfo = matchStatusService.getMatchHeadUserInfo(userSession.getId(), subject);
        return matchHeadUserInfo;
    }
}
