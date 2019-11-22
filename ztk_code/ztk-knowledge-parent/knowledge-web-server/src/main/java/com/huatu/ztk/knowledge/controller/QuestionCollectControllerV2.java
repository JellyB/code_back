package com.huatu.ztk.knowledge.controller;

import com.huatu.ztk.commons.PageBean;
import com.huatu.ztk.commons.TerminalType;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.knowledge.api.SubjectDubboService;
import com.huatu.ztk.knowledge.service.QuestionCollectService;
import com.huatu.ztk.knowledge.util.PageUtil;
import com.huatu.ztk.user.service.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * 问题收藏控制器
 */

@RestController
@RequestMapping(value = "/v2/collects",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class QuestionCollectControllerV2 {
    public static final Logger logger = LoggerFactory.getLogger(QuestionCollectControllerV2.class);

    @Autowired
    private QuestionCollectService questionCollectService;


    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private SubjectDubboService subjectDubboService;



    /**
     * 通过userId查询该用户的收藏问题列表（分页）
     * @param token
     * @return
     */
    @RequestMapping(value = "/",method = RequestMethod.GET)
    public Object query(@RequestHeader(required = false) String token,
                        @RequestHeader int terminal,
                        @RequestParam int pointId,
                        @RequestParam(name = "page", defaultValue = "1") int page,
                        @RequestParam(name = "pageSize", defaultValue = "20")int pageSize) throws BizException{
        userSessionService.assertSession(token);
        //取得用户ID
        long userId = userSessionService.getUid(token);
        //fix ios 客户端分页问题
        if(TerminalType.PC != terminal){
            pageSize = 5000;
        }
        PageUtil<Integer> pageBean = questionCollectService.findByPointPage(pointId,userId,page,pageSize);
        return pageBean;
    }


}
