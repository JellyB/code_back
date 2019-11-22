package com.huatu.ztk.user.controller;

import com.huatu.ztk.user.bean.UserDto;
import com.huatu.ztk.user.service.EssayStatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @Author jbzm
 * @Date Create on 2018/1/6 20:12
 */
@RestController
@RequestMapping("essay/statistics")
@Deprecated
public class EssayStatisticsController {
    private static final Logger logger = LoggerFactory.getLogger(EssayStatisticsController.class);
    @Autowired
    private EssayStatisticsService essayStatisticsService;

    @RequestMapping(value = "user", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<UserDto> user(@RequestBody List<UserDto> userList, HttpServletRequest httpServletRequest) {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}", url, remoteAddr);
        logger.info("开始获取对象");
        return essayStatisticsService.findUserById(userList);
    }
}
