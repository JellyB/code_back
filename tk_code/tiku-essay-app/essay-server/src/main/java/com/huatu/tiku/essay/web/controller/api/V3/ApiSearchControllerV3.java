package com.huatu.tiku.essay.web.controller.api.V3;

import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.service.EssaySearchService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.vo.resp.SearchRespVO;
import com.huatu.tiku.springboot.users.support.Token;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhaoxi
 * 申论搜索V3
 */
@RestController
@RequestMapping(value = "api/v3/search", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class ApiSearchControllerV3 {
    @Autowired
    private EssaySearchService essaySearchService;


    /**
     * 根据关键词搜索单题组
     *
     * @param userSession
     * @param content
     * @param page
     * @param pageSize
     * @return appType app类型（1华图在线 2 教师网）
     */
    @LogPrint
    @GetMapping("")
    public Object searchQuestion(@Token(check = false) UserSession userSession,
                                 @RequestParam(defaultValue = "") String content,
                                 @RequestParam(defaultValue = "-1") int type,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "3") int pageSize,
                                 @RequestHeader String cv,
                                 @RequestHeader int terminal,
                                 @RequestHeader(defaultValue = "1") int appType) {

        //只要前十个字符后面多余的失效
        if (content.length() > 10) {
            content = content.substring(0, 10);
        }
        int userId = (userSession == null) ? -1 : userSession.getId();
        List<SearchRespVO> ret = (List<SearchRespVO>) essaySearchService.searchQuestionV3(userId, content, page, pageSize, type);
        //AppType (1 华图在线 2 教师网),教师网不上报神策
        if (appType == 1 && userSession != null) {
            essaySearchService.report2Sensors(userSession.getUcId(), ret.size(), content, terminal);
        }
        return ret;
    }

}
