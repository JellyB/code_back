package com.huatu.ztk.knowledge.controller;

import com.huatu.ztk.commons.PageBean;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.knowledge.service.QuestionErrorService;
import com.huatu.ztk.knowledge.util.PageUtil;
import com.huatu.ztk.user.service.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/v3/errors",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class QuestionErrorControllerV3 {
    private static final Logger logger = LoggerFactory.getLogger(QuestionErrorControllerV3.class);

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private QuestionErrorService questionErrorService;

    /**
     * 查看错题列表（升级成分页接口）
     * @param token
     * @param pointId 知识点
     * @return
     */
    @RequestMapping(value = "/",method = RequestMethod.GET)
    public Object gets(@RequestHeader(required = false) String token,
                       @RequestParam(name = "page", defaultValue = "1") int page,
                       @RequestParam(name = "pageSize", defaultValue = "20")int pageSize,
                       @RequestParam int pointId
                      ) throws BizException {
        userSessionService.assertSession(token);
        //取得用户ID
        long userId = userSessionService.getUid(token);
        PageUtil<Integer> pageBean = questionErrorService.findByPointV2(pointId, userId, page,pageSize);

        return pageBean;
    }
}
