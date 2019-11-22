package com.huatu.tiku.banckend.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.huatu.common.SuccessMessage;
import com.huatu.tiku.banckend.service.AdvertService;
import com.huatu.tiku.common.AdvertEnum;
import com.huatu.tiku.entity.Advert;
import com.huatu.tiku.teacher.service.subject.TeacherSubjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 广告管理
 * Created by lijun on 2018/5/31
 */
@Slf4j
@RestController
@RequestMapping("/backend/advert")
public class AdvertController {


    @Autowired
    private AdvertService advertService;

    @Autowired
    private TeacherSubjectService teacherSubjectService;


    /**
     * 获取category 下所有的 subject
     * @param category
     * @return
     */
    @GetMapping(value = "category")
    public Object selectByCategory(long category){
        return teacherSubjectService.findChildren(category, 1);
    }
    /**
     * 全条件列表查询
     *
     * @return
     */
    @GetMapping(value = "getAdvertByAllConditions")
    public Object getAdvertByAllConditions(
            @RequestParam(required = false, defaultValue = "") String title,
            @RequestParam(required = false, defaultValue = "") String target,
            @RequestParam(required = false, defaultValue = "0") int type,
            @RequestParam(required = false, defaultValue = "-1") int status,
            @RequestParam(required = false, defaultValue = "0") int category,
            @RequestParam(required = false, defaultValue = "0") String platForm,
            @RequestParam(required = false, defaultValue = "0") int subject,
            @RequestParam(required = false, defaultValue = "0") int cateId,
            @RequestParam(required = false, defaultValue = "0") int appType,
            @RequestParam(required = false, defaultValue = "0") Long onLineTime,
            @RequestParam(required = false, defaultValue = "0") Long offLineTime,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false, defaultValue = "1") int page
    ) {
        PageInfo<Advert> pageInfo = PageHelper.startPage(page, pageSize)
                .doSelectPageInfo(() -> advertService.getAdvertByAllConditions(title, target, type, status, category, appType, onLineTime, offLineTime, platForm, subject, cateId));
        return pageInfo;
    }

    /**
     * 新增/修改 数据
     *
     * @return
     */
    @PostMapping(value = "/edit")
    public Object edit(
            @RequestParam(required = false) String file,
            @RequestParam(required = false) String padImageUrl,
            @RequestParam(required = false, defaultValue = "0") int id,
            @RequestParam String title,
            @RequestParam (required = false, defaultValue = "") String target,
            @RequestParam(defaultValue = "") String params,
            @RequestParam int type,
            @RequestParam int category,
            @RequestParam(defaultValue = "0") int newVersion,
            @RequestParam(defaultValue = "2") int appType,
            @RequestParam(defaultValue = "0") int index,
            @RequestParam(defaultValue = "0", required = false) Long onLineTime,
            @RequestParam(defaultValue = "-1", required = false) Long offLineTime,
            @RequestParam(defaultValue = "0") int position,
            @RequestParam(defaultValue = "0") long courseCollectionId,
            @RequestParam(value = "platForm", defaultValue = "") String platForm,
            @RequestParam(value = "subject", defaultValue = "0") int subject,
            @RequestParam(value = "cateId", required = false, defaultValue = "0") int cateId,
            @RequestParam(value = "mid", defaultValue = "0") int mId,
            @RequestParam(value = "mtitle", defaultValue = "") String mTitle

    ) {
        advertService.save(id, target, title, params, category, type, file, newVersion, position, appType, onLineTime, offLineTime, index, courseCollectionId, padImageUrl, platForm, subject, cateId, mId, mTitle);
        return SuccessMessage.create("保存成功");
    }

    /**
     * 删除数据
     *
     * @param id 删除ID
     * @return
     */
    @DeleteMapping(value = "/{id}")
    public Object delete(@PathVariable("id") int id) {
        advertService.delete(id);
        return SuccessMessage.create("删除成功");
    }

    /**
     * 修改上线/下线状态
     *
     * @param id 修改ID
     * @return
     */
    @PutMapping(value = "/{id}")
    public Object updateStatus(@PathVariable("id") int id) {
        advertService.updateStatus(id);
        return SuccessMessage.create("修改成功");
    }

    /**
     * 通过ID 查询详情
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/{id}")
    @ResponseBody
    public Object detail(
            @PathVariable("id") int id
    ) {
        return advertService.detail(id);
    }

    /**
     * 获取  广告类型
     *
     * @return
     */
    @GetMapping(value = "getTypeList")
    public Object getSubjectList() {
        return AdvertEnum.Type.toList();
    }

    /**
     * 获取科目
     *
     * @return
     */
    @GetMapping(value = "getCategory")
    public Object getCategory() {
        return AdvertEnum.Category.toList();
    }

    /**
     * 根据科目获取 页面类型
     *
     * @param code
     * @param isM 是否为 m 站
     * @return
     */
    @GetMapping(value = "getTargetByCategory/{code}")
    public Object getTargetByCategory(@PathVariable("code") int code,
                                      @RequestParam(value = "m", defaultValue = "0") int  isM) {
        if(isM > 0){
            return AdvertEnum.MTarget.getTargetByCategory();
        }
        return AdvertEnum.Target.getTargetByCategory(code);
    }
}
