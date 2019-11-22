package com.huatu.tiku.interview.controller.admin.v1;

/**
 * Created by x6 on 2018/5/17.
 */

import com.huatu.tiku.interview.entity.result.Result;
import com.huatu.tiku.interview.service.SignService;
import com.huatu.tiku.interview.util.LogPrint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

/**
 * 班级管理
 * @Author ZhenYang
 * @Date Created in 2018/1/25 19:59
 * @Description
 */
@RestController
@RequestMapping("/end/sign")
public class SignController {
    @Autowired
    private SignService signService;

    /**
     * 查询用户签到信息
     */
    @LogPrint
    @GetMapping
    public Result find(@RequestParam(name = "page", defaultValue = "1") int page,
                           @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                           @RequestParam(name = "classId",defaultValue = "-1") long classId,
                           @RequestParam(name = "uname",defaultValue = "") String uname
    ) {
        return Result.ok(signService.findByConditions(page,pageSize,uname,classId));
    }


    /**
     * 用户签到信息导出
     */
    @LogPrint
    @PostMapping
    public ModelAndView export(
                       @RequestParam(name = "classId",defaultValue = "-1") long classId,
                       @RequestParam(name = "uname",defaultValue = "") String uname
    ) {
        return signService.export(uname,classId);
    }


    /**
     * 用户签到信息导出
     */
    @LogPrint
    @GetMapping("export")
    public ModelAndView exportTest(
            @RequestParam(name = "classId",defaultValue = "-1") long classId,
            @RequestParam(name = "uname",defaultValue = "") String uname
    ) {
        return signService.export(uname,classId);
    }

}
