package com.huatu.tiku.interview.controller.admin.v1;

import com.huatu.tiku.interview.entity.result.Result;
import com.huatu.tiku.interview.service.LearningSituationMaterialService;
import com.huatu.tiku.interview.util.LogPrint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by x6 on 2018/4/11.
 * 联系内容相关素材管理
 */
@RestController
@Slf4j
@RequestMapping(value = "/end/material", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class LearningSituationMaterialController {

    @Autowired
    LearningSituationMaterialService learningSituationMaterialService;

    /**
     * 查询练习内容 （模块）
     */
    @LogPrint
    @GetMapping("practiceContent")
    public Result practiceContent() {

        return Result.ok(learningSituationMaterialService.getPracticeContent());
    }


    /**
     * 查询优点及问题
     */
    @LogPrint
    @GetMapping("remark/{type}")
    public Result remark(@PathVariable long type) {
        return Result.ok(learningSituationMaterialService.getRemarkList(type));
    }


    /**
     * 查询词库数据
     */
    @LogPrint
    @GetMapping("word")
    public Result word() {
        return Result.ok(learningSituationMaterialService.getWordList());
    }

    /**
     * 查询表现相关数据
     */
    @LogPrint
    @GetMapping("expression")
    public Result expression() {
        return Result.ok(learningSituationMaterialService.getExpressionList());
    }


    /**
     * 查询试卷信息
     */
    @LogPrint
    @GetMapping("paper")
    public Result paper() {
        return Result.ok(learningSituationMaterialService.getPaperDetail());
    }


}
