package com.huatu.ztk.paper.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 错题相关功能
 * Created by shaojieyue
 * Created time 2016-06-15 09:47
 */
@RestController
@RequestMapping(value = "/v1/practices/",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class ErrorQuestionControllerV1 {
    private static final Logger logger = LoggerFactory.getLogger(ErrorQuestionControllerV1.class);


}
