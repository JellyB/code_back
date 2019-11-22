package com.huatu.tiku.teacher.controller.admin.paper;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.huatu.common.SuccessMessage;
import com.huatu.tiku.entity.teacher.PaperAssembly;
import com.huatu.tiku.enums.BaseInfo;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.teacher.service.paper.PaperAssemblyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by lijun on 2018/8/16
 */
@RestController
@RequestMapping("paperAssembly")
public class PaperAssemblyController {

    @Autowired
    private PaperAssemblyService service;

    /**
     * 保存试卷信息
     */
    @PostMapping
    public Object save(@RequestBody PaperAssembly paperAssembly) {
        paperAssembly.setType(PaperInfoEnum.PaperAssemblyType.MANUAL.getCode());
        service.savePaperAssemblyInfo(paperAssembly);
        return SuccessMessage.create("操作成功");
    }

    /**
     * 列表查询
     */
    @GetMapping
    public Object list(
            @RequestParam(defaultValue = BaseInfo.SEARCH_INPUT_DEFAULT) String name,
            @RequestParam(defaultValue = BaseInfo.SEARCH_INPUT_DEFAULT) String beginTime,
            @RequestParam(defaultValue = BaseInfo.SEARCH_INPUT_DEFAULT) String endTime,
            @RequestParam (defaultValue = BaseInfo.SEARCH_DEFAULT)Long subjectId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        PageInfo<PaperAssembly> pageInfo = PageHelper.startPage(page, pageSize)
                .doSelectPageInfo(() -> service.list(name, beginTime, endTime,subjectId, PaperInfoEnum.PaperAssemblyType.MANUAL));
        return pageInfo;
    }

    /**
     * 详情
     */
    @GetMapping("{id}")
    public Object detail(@PathVariable("id") long id) {
        return service.detailWithQuestion(id);
    }

    /**
     * 删除组卷
     */
    @DeleteMapping("{id}")
    public Object deleteAssembly(@PathVariable("id") Long id) {
        service.deleteAssembly(id);
        return SuccessMessage.create("删除成功");
    }
}
