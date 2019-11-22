package com.huatu.ztk.user.controller;

import com.huatu.ztk.user.service.IconService;
import com.huatu.ztk.user.service.UserSessionService;
import com.huatu.ztk.user.util.UserTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-10-25 1:29 PM
 **/

@RestController
@RequestMapping(value = "icon")
public class IconController {


    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private IconService iconService;

    /**
     * 获取科目下 icon 配置
     * @param subject
     * @param token
     * @param subjectId
     * @return
     */
    @RequestMapping
    public Object list(@RequestHeader Integer subject,
                       @RequestHeader String token,
                       @RequestParam Integer subjectId){

        int subject_ = UserTokenUtil.getHeaderSubject(token, userSessionService::getSubject, subjectId, subject);
        return iconService.list(subject_);
    }
}
