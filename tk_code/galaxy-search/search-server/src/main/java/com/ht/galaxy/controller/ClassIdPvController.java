package com.ht.galaxy.controller;

import com.ht.galaxy.service.LogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author gaoyuchao
 * @create 2018-08-06 9:52
 */
@RestController
public class ClassIdPvController {

    @Autowired
    private LogsService logsService;

    @RequestMapping("/bj/logs/select")
    public Object selectClassCvr(String classId) throws Exception {
        return logsService.selectClassCvr(classId);
    }

}
