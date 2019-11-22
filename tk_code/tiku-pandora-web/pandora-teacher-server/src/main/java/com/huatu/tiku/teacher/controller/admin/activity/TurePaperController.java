package com.huatu.tiku.teacher.controller.admin.activity;

import com.google.common.collect.Lists;
import com.huatu.tiku.teacher.service.activity.TruePaperService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by huangqingpeng on 2019/1/28.
 */
@RestController
@Slf4j
@RequestMapping("truePaper")
public class TurePaperController {

    @Autowired
    TruePaperService truePaperService;

    /**
     * 导出活动卷中的试题知识点数量分布表格
     */
    @GetMapping("knowledge/{activityId}")
    public Object getKnowledgeExcel(@PathVariable long activityId) {
        String s = truePaperService.handlerKnowledgeExcelById(activityId);
        return Lists.newArrayList(s);
    }

    /**
     * 导出多个活动试卷中的试题知识点数量分布表
     */
    @GetMapping("knowledge")
    public Object getKnowledgeExcels(@RequestParam String ids) {
        List<String> collect = Arrays.stream(ids.split(",")).map(Long::parseLong).map(truePaperService::handlerKnowledgeExcelById)
                .collect(Collectors.toList());
        return collect;
    }

    /**
     * 按年份科目和活动类型批量导出多个活动中的试题知识点分布表
     */
    @GetMapping("knowledge/range")
    public Object getKnowledgeExcelsByRange(@RequestParam int year,
                                            @RequestParam int subject,
                                            @RequestParam int activityType) {

        return truePaperService.   handlerKnowledgeExcelsByRange(year,subject,activityType);
    }
}
