package com.huatu.tiku.essay.web.controller.api.V1;

/**
 * Created by x6 on 2018/3/22.
 */

import com.huatu.tiku.essay.service.EssayReadFileService;
import com.huatu.tiku.essay.util.LogPrint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 套题
 * Created by huangqp on 2017\11\23 0023.
 */
@RestController
@RequestMapping("api/v1/read")
@Slf4j
public class ApiReadFileController {

    @Autowired
    EssayReadFileService essayReadFileService;
    /**
     * 读取txt生成Word文档
     */
    @LogPrint
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public boolean file(@RequestParam(name = "fileName", defaultValue = "")String fileName) {
        return essayReadFileService.createFile(fileName);
    }



    /**
     * 读取txt插入三级地区
     */
    @LogPrint
    @GetMapping(value = "area", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public boolean area() {
        return essayReadFileService.area();
    }







}
