package com.huatu.tiku.interview.controller.admin.v1;

import com.huatu.tiku.interview.entity.result.Result;
import com.huatu.tiku.interview.service.ClassInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 班级管理
 * @Author ZhenYang
 * @Date Created in 2018/1/25 19:59
 * @Description
 */
@RestController
@RequestMapping("/end/class")
public class ClazzController {

    @Autowired
    private ClassInfoService classInfoService;

    @GetMapping("list")
    public Result getList(){

        return Result.ok(classInfoService.getList());
    }
}
