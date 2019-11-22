package com.huatu.tiku.essay.web.controller.admin;

import com.ht.base.start.security.service.UserOption;
import com.huatu.tiku.essay.service.EssayTeacherService;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.vo.req.CreateOrUpdateTeacherReq;
import com.huatu.tiku.essay.vo.req.FetchTeacherReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * Created by duanxiangchao on 2019/7/10
 */
@RestController
@Slf4j
@RequestMapping("/end/teacher")
public class EssayTeacherController {


    @Resource
    private UserOption userOption;
    @Resource
    private EssayTeacherService essayTeacherService;


    @GetMapping("/ucenter/list")
    public Object list(String name) {
        return essayTeacherService.listTeacher(name);
    }

    @PostMapping(value = "save", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object save(@RequestBody @Validated CreateOrUpdateTeacherReq teacherReq) {
        return essayTeacherService.addOrUpdateTeacher(teacherReq);
    }

    @GetMapping("/order/list")
    public PageUtil listOrder(Long teacherId,
                              @RequestParam(defaultValue = "1") Integer page,
                              @RequestParam(defaultValue = "20") Integer pageSize) {
        return essayTeacherService.fetchCorrectOrder(teacherId, page, pageSize);
    }

    @GetMapping("/list")
    public PageUtil list(FetchTeacherReq fetchTeacherReq) {
        return essayTeacherService.fetchTeacher(fetchTeacherReq);
    }

    @GetMapping("/detail/{teacherId}")
    public Object detail(@PathVariable Long teacherId) {
        return essayTeacherService.teacherDetail(teacherId);
    }

    /**
     * 任务分配老师列表
     *
     * @param fetchTeacherReq
     * @return
     */
    @GetMapping("/distribution/list")
    public PageUtil distributionList(FetchTeacherReq fetchTeacherReq) {
        return essayTeacherService.fetchDistributionTeacher(fetchTeacherReq);
    }


}
