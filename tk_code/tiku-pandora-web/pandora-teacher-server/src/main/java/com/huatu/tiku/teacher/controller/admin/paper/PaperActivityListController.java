package com.huatu.tiku.teacher.controller.admin.paper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.huatu.tiku.entity.teacher.PaperModuleInfo;
import com.huatu.tiku.enums.EnumUtil;
import com.huatu.tiku.teacher.enums.ActivityLookParseType;
import com.huatu.tiku.teacher.service.PaperActivityService;
import com.huatu.tiku.teacher.service.impl.knowledge.KnowledgeSubjectInstance;
import com.huatu.tiku.teacher.service.paper.PaperActivityListService;
import com.huatu.tiku.util.log.LogPrint;
import com.huatu.ztk.commons.exception.SuccessMessage;

import lombok.extern.slf4j.Slf4j;

/**
 * @创建人 lizhenjuan
 * @创建时间 2018/8/11
 * @描述 活动列表页面的相关操作
 */

@Slf4j
@RestController
@RequestMapping("activityList")
public class PaperActivityListController {


    @Autowired
    PaperActivityService paperActivityService;

    @Autowired
    PaperActivityListService paperActivityListService;

    /**
     * @param subjectId
     * @param type
     * @param bizStatus
     * @param year
     * @param name
     * @param searchType 1 按照名称搜索,2 按照ID搜索
     * @return
     */
    @LogPrint
    @GetMapping("list")
    public Object list(@RequestParam(defaultValue = "-1") Long subjectId,
                       @RequestParam(defaultValue = "-1") Integer type,
                       @RequestParam(defaultValue = "-1") Integer bizStatus,
                       @RequestParam(defaultValue = "-1") Integer year,
                       @RequestParam(defaultValue = "") String areaIds,
                       @RequestParam(defaultValue = "") String name,
                       @RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(defaultValue = "1") int searchType) {
        //科目转换 - 匹配事业单位 跨科目查询试卷

        Long parentSubject = KnowledgeSubjectInstance.getInstance().transChildrenSubjectToBase(subjectId);
        Set<Integer> subjectIds = new HashSet<>();
        //兼容事业单位，查询本科目ID以及父科目ID
        subjectIds.add(parentSubject.intValue());
        subjectIds.add(subjectId.intValue());
        log.info("查询活动列表,科目参数是:{}", subjectIds);

        PageInfo<List<HashMap<String, Object>>> mapPageInfo = PageHelper.startPage(page, size)
                .doSelectPageInfo(() -> paperActivityService.getActivityList(type,
                        bizStatus, year, areaIds, name.trim(), new ArrayList<>(subjectIds), "", "", searchType));

        return mapPageInfo;
    }


    /**
     * 修改活动发布状态
     * id 活动ID
     */
    @PutMapping("paperStatus/{id}")
    public Object updatePaperStatus(@PathVariable Long id) {
        paperActivityService.updatePaperStatus(id);
        return SuccessMessage.create("状态修改成功！");
    }



    /**
     * 修改题源标识
     */
    @PutMapping("sourceFlag/{id}")
    public Object updateSourceFlag(@PathVariable Long id) {
        paperActivityService.updateSourceFlag(id);
        return com.huatu.common.SuccessMessage.create("操作成功");
    }

    /**
     * 活动列表，查询报名人数，参加人数
     * 科目默认是行测
     *
     * @param id 活动ID TODO 需要做统计
     */
    @LogPrint
    @GetMapping("activityData")
    public Object activityData(@RequestParam Long id,
                               @RequestParam(defaultValue = "1") Long subjectId) {
        return paperActivityService.activityData(id, subjectId);
    }


    /**
     * 查询一张试卷
     *
     * @param id 活动id
     */
    @LogPrint
    @GetMapping("activityDetail")
    public Object getActivityDetail(@RequestParam Long id) {
        return paperActivityService.paperDetail(id);
    }

    /**
     * 添加活动-下拉框-查看解析状态
     */
    @LogPrint
    @GetMapping("activityLookParseType")
    public Object getActivityLookParseType() {
        return EnumUtil.asList(ActivityLookParseType.class);
    }


    /**
     * 添加模块
     */
    @LogPrint
    @PostMapping("saveModule/{activityId}")
    public Object saveModule(@PathVariable Long activityId,
                             @RequestBody List<String> moduleNames) {
        paperActivityListService.saveModuleInfo(activityId, moduleNames);
        return SuccessMessage.create("添加模块成功！");
    }

    /**
     * 删除模块信息
     */
    @DeleteMapping("deleteModule/{activityId}")
    public Object deleteModule(@PathVariable long activityId,
                               @RequestBody List<Integer> moduleIds) {
        paperActivityListService.deleteModuleInfoByIdList(activityId, moduleIds);
        return com.huatu.common.SuccessMessage.create("删除成功");
    }


    /**
     * 修改模块信息
     */
    @PutMapping("updateModule/{activityId}")
    public Object updateModule(@PathVariable long activityId,
                               @RequestBody List<PaperModuleInfo> moduleInfoList) {
        paperActivityListService.updateModuleInfo(activityId, moduleInfoList);
        return com.huatu.common.SuccessMessage.create("修改成功");
    }

    /**
     * 切换模块位置
     */
    @PutMapping("changeModule/{activityId}")
    public Object changeModule(
            @PathVariable long activityId,
            @RequestBody List<PaperModuleInfo> moduleInfoList
    ) {
        paperActivityListService.changeModuleInfo(activityId, moduleInfoList);
        return com.huatu.common.SuccessMessage.create("操作成功");
    }

    /**
     * 活动标签
     *
     * @param subjectId
     * @return
     */
    @GetMapping("tags/{subjectId}/{level}")
    public Object getTags(@PathVariable Long subjectId, @PathVariable Integer level) {
        return paperActivityService.getTags(subjectId, level);
    }

}
