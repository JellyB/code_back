package com.huatu.ztk;

import com.huatu.ztk.user.service.NewAdvertService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhaoxi
 * @Description: 测试pandora数据库接入
 * @date 2018/9/3下午1:57
 */
@Slf4j
@RestController
@RequestMapping(value = "v1/test", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class TestController {

    @Autowired
    private NewAdvertService service;

    @RequestMapping(value = "all", method = RequestMethod.GET)
    public Object test() {

        log.info("测试pandora数据库：all");
        return service.selectAll();
    }
}
