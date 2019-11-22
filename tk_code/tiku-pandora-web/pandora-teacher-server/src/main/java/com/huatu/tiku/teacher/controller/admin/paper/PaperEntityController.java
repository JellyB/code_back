package com.huatu.tiku.teacher.controller.admin.paper;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.huatu.common.SuccessMessage;
import com.huatu.common.utils.collection.HashMapBuilder;
import com.huatu.tiku.entity.teacher.PaperEntity;
import com.huatu.tiku.entity.teacher.PaperModuleInfo;
import com.huatu.tiku.enums.BaseInfo;
import com.huatu.tiku.enums.EnumUtil;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.teacher.service.impl.knowledge.KnowledgeSubjectInstance;
import com.huatu.tiku.teacher.service.paper.PaperEntityService;
import com.huatu.tiku.teacher.util.personality.PersonalityAreaUtil;
import com.huatu.tiku.util.log.LogPrint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tk.mybatis.mapper.entity.Example;

import java.beans.PersistenceDelegate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 实体试卷信息处理 - 试卷中心 相关接口
 * Created by lijun on 2018/7/31
 */
@Slf4j
@RestController
@RequestMapping("paperEntity")
public class PaperEntityController {

    @Autowired
    private PaperEntityService service;

    /**
     * 列表查询
     *
     * @param mode        试卷属性
     * @param year        年份
     * @param specialFlag 是否是特等教师
     * @param missFlag    是否残缺
     * @param subjectId   科目ID
     * @param bizStatus   试卷状态
     * @param area     区域ID 数组
     * @param name        试卷名称
     */
    @LogPrint
    @GetMapping("list")
    public Object list(
            @RequestParam(defaultValue = BaseInfo.SEARCH_DEFAULT) int mode,
            @RequestParam(defaultValue = BaseInfo.SEARCH_DEFAULT) int year,
            @RequestParam(defaultValue = BaseInfo.SEARCH_DEFAULT) int specialFlag,
            @RequestParam(defaultValue = BaseInfo.SEARCH_DEFAULT) int missFlag,
            @RequestParam(defaultValue = BaseInfo.SEARCH_DEFAULT) long subjectId,
            @RequestParam(defaultValue = BaseInfo.SEARCH_DEFAULT) int bizStatus,
            @RequestParam(defaultValue = BaseInfo.SEARCH_DEFAULT) String area,
            @RequestParam(defaultValue = BaseInfo.SEARCH_INPUT_DEFAULT) String name,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {

        //科目转换 - 匹配事业单位 跨科目查询试卷
        Long parentSubject = KnowledgeSubjectInstance.getInstance().transChildrenSubjectToBase(subjectId);
        PageInfo<HashMap<String, Object>> pageInfo = PageHelper.startPage(page, pageSize)
                .doSelectPageInfo(
                        () -> service.list(mode, year, specialFlag, missFlag, parentSubject, bizStatus, area, name.trim())
                );
        List<HashMap<String, Object>> collect = pageInfo.getList().stream()
                .map(map -> {
                    //基础信息转换
                    map.computeIfPresent("mode", (key, value) -> EnumUtil.valueOf(Integer.valueOf(String.valueOf(value)), PaperInfoEnum.ModeEnum.class));
                    map.computeIfPresent("missFlag", (key, value) -> EnumUtil.valueOf(Integer.valueOf(String.valueOf(value)), BaseInfo.YESANDNO.class));
                    map.computeIfPresent("bizStatus", (key, value) -> EnumUtil.valueOf(Integer.valueOf(String.valueOf(value)), PaperInfoEnum.BizStatus.class));
                    //处理地区可能不存在的情况
                    map.computeIfPresent("area",(a,b)-> PersonalityAreaUtil.getAreaName((int)subjectId,-1,String.valueOf(b)));
                    map.computeIfAbsent("area", (key) -> "信息缺失");
                    map.computeIfAbsent("sourceFlag", (key) -> "0");
                    return map;
                })
                .collect(Collectors.toList());
        pageInfo.setList(collect);
        return pageInfo;
    }

    /**
     * 数据保存 - 根据有无ID判断是否为新存
     */
    @PostMapping
    public Object save(@RequestBody PaperEntity paperEntity) {
        if(null == paperEntity.getSubjectId() && Optional.ofNullable(paperEntity.getId()).orElse(-1L) > 0){
            Example example = new Example(PaperEntity.class);
            example.and().andEqualTo("id",paperEntity.getId());
            service.updateByExampleSelective(paperEntity,example);
            return paperEntity;
        }
        Long parentSubject = KnowledgeSubjectInstance.getInstance().transChildrenSubjectToBase(paperEntity.getSubjectId());
        paperEntity.setSubjectId(parentSubject);
        service.savePaper(paperEntity);
        return paperEntity;
    }

    /**
     * 删除实体卷数据
     */
    @DeleteMapping("{paperId}")
    public Object delete(@PathVariable long paperId) {
        service.deletePaper(paperId);
        return SuccessMessage.create("操作成功");
    }

    /**
     * 获取试卷详情
     */
    @GetMapping("{paperId}")
    public Object detail(@PathVariable long paperId) {
        return service.detail(paperId);
    }

    /**
     * 修改发布状态
     */
    @PutMapping("{paperId}/releaseStatus")
    public Object updateReleaseStatus(@PathVariable long paperId) {
        service.updateReleaseStatus(paperId);
        return SuccessMessage.create("操作成功");
    }

    /**
     * 修改题源标识
     */
    @PutMapping("{paperId}/sourceFlag")
    public Object updateSourceFlag(@PathVariable long paperId) {
        service.updateSourceFlag(paperId);
        return SuccessMessage.create("操作成功");
    }

    /**
     * 试卷数据详情-统计数据
     */
    @GetMapping("{paperId}/metaData")
    public Object getMetaData(@PathVariable long paperId) {
        return HashMapBuilder.<String, Object>newBuilder()
                .put("count", -1L)
                .put("average", -1.1D)
                .put("difficult", -1.1D)
                .build();
    }

    /**
     * 新增模块信息
     */
    @PostMapping("{paperId}/saveModule")
    public Object saveModule(
            @PathVariable long paperId,
            @RequestBody List<String> moduleNameList
    ) {
        return service.saveModuleInfo(paperId, moduleNameList);
    }

    /**
     * 删除模块信息
     */
    @DeleteMapping("{paperId}/deleteModule")
    public Object deleteModule(
            @PathVariable long paperId,
            @RequestBody List<Integer> moduleNameList
    ) {
        service.deleteModuleInfoByIdList(paperId, moduleNameList);
        return SuccessMessage.create("操作成功");
    }

    /**
     * 修改模块信息
     */
    @PutMapping("{paperId}/updateModule")
    public Object updateModule(
            @PathVariable long paperId,
            @RequestBody List<PaperModuleInfo> moduleInfoList
    ) {
        service.updateModuleInfo(paperId, moduleInfoList);
        return SuccessMessage.create("操作成功");
    }


    /**
     * 切换模块位置
     */
    @PutMapping("{paperId}/changeModule")
    public Object changeModule(
            @PathVariable long paperId,
            @RequestBody List<PaperModuleInfo> moduleInfoList
    ) {
        service.changeModuleInfo(paperId, moduleInfoList);
        return SuccessMessage.create("操作成功");
    }
}
