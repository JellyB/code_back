package com.huatu.tiku.course.web.controller;


import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.ExamNetSchoolService;
import com.huatu.tiku.course.service.exam.ExamService;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.springboot.users.support.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/2/15
 * @描述 备考精华
 */

@RestController
@RequestMapping(value = "v1/exam")
@Slf4j
public class ExamController {


    @Autowired
    ExamNetSchoolService examNetSchoolService;

    @Autowired
    ExamService examService;

    /**
     * 获取备考精华文章列表
     *
     * @param type
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping(value = "{type}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object getArticleList(@PathVariable int type,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "10") int pageSize,
                                 @Token(check = false, defaultValue = "") UserSession userSession) {
        int subject = userSession == null ? 1 : userSession.getSubject();
        return examService.getArticleList(type, page, pageSize, subject);
    }

    /**
     * 获取文章详情
     *
     * @param aid
     * @return
     */
    @GetMapping(value = "detail/{aid}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object detail(@PathVariable int aid) {
        return examService.detail(aid);
    }

    /**
     * 用户点赞
     *
     * @param aid
     * @param type 1 点赞;-1取消点赞
     * @return
     */
    @PostMapping(value = "like/{aid}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object like(@PathVariable int aid,
                       @RequestParam(defaultValue = "1") int type) {

        HashMap paramMap = new HashMap();
        paramMap.put("id", aid);
        paramMap.put("type", type);
        NetSchoolResponse like = examService.like(paramMap);
        return ResponseUtil.build(like);
    }

    /**
     * 备考精华分类列表
     *
     * @return
     */
    @GetMapping(value = "typeList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object typeList() {
        return examService.typeList();
    }
}
