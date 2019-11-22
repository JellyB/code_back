package com.huatu.tiku.teacher.controller.admin.tag;

import com.github.pagehelper.PageHelper;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.constants.teacher.TeacherErrors;
import com.huatu.tiku.entity.tag.QuestionTag;
import com.huatu.tiku.entity.tag.Tag;
import com.huatu.tiku.teacher.service.tag.TeacherQuestionTagService;
import com.huatu.tiku.teacher.service.tag.TeacherTagService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @author jbzm
 * @date 2018上午11:19
 **/
@Slf4j
@RestController
@RequestMapping(value = "tag", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class TagController {
    @Autowired
    TeacherTagService teacherTagService;
    @Autowired
    TeacherQuestionTagService teacherQuestionTagService;

    /**
     * 添加或者修改标签（有ID的时候，是修改，否则是添加）
     *
     * @param tag
     * @return
     */
    @PostMapping(value = "")
    public Object saveTag(@RequestBody Tag tag) {
        if (StringUtils.isBlank(tag.getName())) {
            throw new BizException(TeacherErrors.ILLEGAL_PARAM);
        }
        List<Tag> tags = teacherTagService.selectAll();
        //判断标签名是否冲突
        if (CollectionUtils.isNotEmpty(tags)) {
            Tag temp;
            //添加逻辑，确认是存在同名的标签
            if (tag.getId() == null || tag.getId().intValue() <= 0) {
                temp = tags.stream().filter(i -> i.getName().equals(tag.getName())).findFirst().orElse(null);
                if (temp != null) {
                    throw new BizException(ErrorResult.create(100013221, tag.getName() + "标签已存在"));
                }
            } else {
                //修改逻辑，确认是否存在非自己的同名标签
                temp = tags.stream().filter(i -> !i.getId().equals(tag.getId()))
                        .filter(i -> i.getName().equals(tag.getName()))
                        .findFirst().orElse(null);
            }
            if (temp != null) {
                throw new BizException(ErrorResult.create(100013221, tag.getName() + "标签已存在"));
            }
        }
        return teacherTagService.save(tag);
    }

    /**
     * 删除标签
     *
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}")
    public Object delete(@PathVariable Long id) {
        return teacherTagService.deleteByPrimaryKey(id);
    }

    /**
     * 查询标签绑定的试题数量
     *
     * @param id
     * @return
     */
    @GetMapping(value = "questionCount/{id}")
    public Object questionCount(@PathVariable Long id) {
        return teacherQuestionTagService.questionCount(id);
    }


    @GetMapping(value = "all")
    public Object findAll(@RequestParam(defaultValue = "-1") Integer channel,
                          @RequestParam(defaultValue = "") String name,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int pageSize) {

        return PageHelper.startPage(page, pageSize)
                .doSelectPageInfo(
                        () -> teacherTagService.list(channel, name)
                );

    }
}
