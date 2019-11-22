package com.huatu.ztk.user.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by linkang on 8/8/16.
 */
@RestController
public class ServerCheckController {
    private static final Logger logger = LoggerFactory.getLogger(ServerCheckController.class);
    /**
     * 空接口，检测服务器状态
     */
    @RequestMapping(value = "checkServer")
    public void check(HttpServletRequest httpServletRequest) {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}", url, remoteAddr);
    }
}
