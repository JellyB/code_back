package com.huatu.ztk.user.controller;

import com.huatu.ztk.user.service.UcenterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Created by lijun on 2018/5/18
 */
@RestController
@Deprecated
@RequestMapping(value = "/netschool/ucenter")
public class UCenterController {

    private final static Logger logger = LoggerFactory.getLogger(UCenterController.class);
    @Autowired
    private UcenterService ucenterService;

    @RequestMapping(value = "pageData", method = RequestMethod.GET)
    public List<Map<String, Object>> pageData(
            @RequestParam(required = false,defaultValue = "0") long beginTime,
            @RequestParam(required = false,defaultValue = "0") long endTime,
            @RequestParam(required = false,defaultValue = "1") int pageNum,
            @RequestParam(required = false,defaultValue = "10") int pageSize,
            HttpServletRequest httpServletRequest) {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}", url, remoteAddr);
        return ucenterService.pageData(beginTime, endTime, pageNum, pageSize);
    }

    @RequestMapping(value = "countNum", method = RequestMethod.GET)
    public long countNum(
            @RequestParam(required = false,defaultValue = "0") long beginTime,
            @RequestParam(required = false,defaultValue = "0") long endTime,
            HttpServletRequest httpServletRequest) {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}", url, remoteAddr);
        return ucenterService.countNum(beginTime, endTime);
    }
}
