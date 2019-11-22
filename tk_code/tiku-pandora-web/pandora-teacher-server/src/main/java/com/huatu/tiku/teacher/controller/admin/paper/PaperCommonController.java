package com.huatu.tiku.teacher.controller.admin.paper;

import com.huatu.tiku.enums.EnumUtil;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.enums.PaperPermission;
import com.huatu.tiku.teacher.service.paper.PaperEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lijun on 2018/8/6
 */
@RestController
@RequestMapping("common/paper")
public class PaperCommonController {

    @Autowired
    private PaperEntityService paperEntityService;

    /**
     * 试卷属性
     */
    @GetMapping("paperModeType")
    public Object paperModeType() {
        return EnumUtil.asList(PaperInfoEnum.ModeEnum.class);
    }

    /**
     * 试卷状态
     */
    @GetMapping("paperStatus")
    public Object paperStatus() {
        return EnumUtil.asList(PaperInfoEnum.BizStatus.class);
    }

    /**
     * 获取各类试卷的权限状态
     */
    @GetMapping("/getPaperPermission/{subjectId}")
    public Object getPaperPermission(@PathVariable long subjectId) {
        return PaperPermission.getAllPermissionBySubjectId(subjectId);
    }

    /**
     * 试题卷考试时间
     */
    @GetMapping("/entityPaperTime")
    public Object getEntityPaperTime() {
        return paperEntityService.getEntityPaperTime();
    }

}
