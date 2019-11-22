package com.huatu.tiku.essay.web.controller.admin;

import com.huatu.tiku.essay.service.EssayAreaService;
import com.huatu.tiku.essay.vo.req.CreateOrUpdateTeacherReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * Created by duanxiangchao on 2019/7/17
 */
@RestController
@Slf4j
@RequestMapping("/end/area")
public class AreaController {

    @Resource
    private EssayAreaService essayAreaService;

    @GetMapping(value = "/tree", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object getAreaTree() {
        return essayAreaService.fetchAreaTree();
    }

}
