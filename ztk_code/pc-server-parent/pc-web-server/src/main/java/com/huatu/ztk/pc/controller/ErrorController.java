package com.huatu.ztk.pc.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * Created by shaojieyue
 * Created time 2016-09-28 10:16
 */
@Controller
@RequestMapping(value = "/error/")
public class ErrorController {
    private static final Logger logger = LoggerFactory.getLogger(ErrorController.class);

    /**
     * 500错误
     * @param code
     * @param message
     * @param model
     * @return
     */
    @RequestMapping(value = "5xx")
    public String bizException(@RequestParam(required = false, defaultValue = "-1") int code,
                               @RequestParam(required = false) String message,
                               HttpServletRequest httpServletRequest,
                               Model model){
        String remoteAddr = httpServletRequest.getRemoteAddr();
        System.out.println("remoteAddr = " + remoteAddr + "接口地址"+httpServletRequest.getRequestURL().toString());
        model.addAttribute("code",code);
        model.addAttribute("message", Optional.ofNullable(message).orElse("请求错误"));
        return "common/500";
    }
}
