package com.huatu.ztk.pc.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by shaojieyue
 * Created time 2016-09-22 16:15
 */

@Controller
@RequestMapping(value = "/system/")
public class SystemController {
    private static final Logger logger = LoggerFactory.getLogger(SystemController.class);

    /**
     * 健康检查接口
     */
    @RequestMapping(value = "health", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public void health(HttpServletRequest httpServletRequest){
        String remoteAddr = httpServletRequest.getLocalAddr();
        System.out.println("httpServletRequest.getRequestURL() = " + httpServletRequest.getRequestURL());
        System.out.println("httpServletRequest.getRequestURI() = " + httpServletRequest.getRequestURI());
        System.out.println("httpServletRequest.getRemoteUser() = " + httpServletRequest.getRemoteUser());
        System.out.println("httpServletRequest.getRemoteAddr() = " + httpServletRequest.getRemoteAddr());
        System.out.println("httpServletRequest.getRemoteHost() = " + httpServletRequest.getRemoteHost());
        System.out.println("httpServletRequest.getRemotePort() = " + httpServletRequest.getRemotePort());
        System.out.println("remoteAddr = " + remoteAddr + "接口地址"+httpServletRequest.getRequestURL().toString());
    }
}
