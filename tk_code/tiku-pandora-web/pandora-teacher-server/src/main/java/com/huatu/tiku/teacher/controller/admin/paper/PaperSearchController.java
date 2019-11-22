package com.huatu.tiku.teacher.controller.admin.paper;

import com.huatu.tiku.enums.BaseInfo;
import com.huatu.tiku.teacher.service.paper.PaperSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 试卷-试题 查询统一处理
 * Created by lijun on 2018/8/8
 */
@RestController
@RequestMapping("basePaper")
public class PaperSearchController {

    @Autowired
    private PaperSearchService paperSearchService;

    /**
     * 查询试题卷接口
     */
    @GetMapping("/{paperId}/entity")
    public Object entityDetail(@PathVariable long paperId) {
        return paperSearchService.entityDetail(paperId);
    }

    /**
     * 实体试卷列表接口
     */
    @GetMapping("entityList")
    public Object entityList(
            @RequestParam(defaultValue = BaseInfo.SEARCH_DEFAULT) int mode,
            @RequestParam(defaultValue = BaseInfo.SEARCH_DEFAULT) int year,
            @RequestParam(defaultValue = BaseInfo.SEARCH_DEFAULT) String areaIds,
            @RequestParam(defaultValue = BaseInfo.SEARCH_INPUT_DEFAULT) String paperTime,
            @RequestParam(defaultValue = BaseInfo.SEARCH_INPUT_DEFAULT) String name,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return paperSearchService.entityList(mode, year, areaIds, paperTime, name, page, pageSize);
    }

}
