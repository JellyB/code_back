package com.huatu.tiku.match.web.controller.v1.share;

import com.huatu.springboot.web.version.mapping.annotation.ApiVersion;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.match.service.v1.share.ShareCreateServer;
import com.huatu.tiku.springboot.users.support.Token;
import com.huatu.ztk.commons.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * 创建模考大赛分享链接
 */
@Slf4j
@RestController
@RequestMapping(value = "share", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@ApiVersion("v1")
public class ShareController {

    @Autowired
    private ShareCreateServer shareCreateServer;

    @PostMapping("")
    public Object matchPractice(@Token UserSession userSession,
                                @RequestParam int paperId,
                                @RequestHeader(defaultValue = "1")  String cv,
                                @RequestHeader int terminal) throws BizException {
        int id = userSession.getId();
        String userName = userSession.getUname();
        String token = userSession.getToken();
        return shareCreateServer.buildShareInfo(paperId,id, userName, token,cv,terminal);
    }
}
