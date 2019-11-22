package com.huatu.tiku.teacher.controller.util;

import com.huatu.tiku.teacher.service.paper.PaperMetaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by huangqingpeng on 2019/1/15.
 */
@Slf4j
@RestController
@RequestMapping("paper/meta")
public class PaperMetaController {

    @Autowired
    PaperMetaService paperMetaService;

    @GetMapping("all")
    public Object syncMetaCache() {
        return paperMetaService.syncMetaCache();
    }
}
