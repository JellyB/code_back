package com.huatu.tiku.teacher.controller.admin.subject;

import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.constants.teacher.TeacherErrors;
import com.huatu.tiku.entity.subject.Subject;
import com.huatu.tiku.teacher.service.subject.TeacherSubjectService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * Created by huangqingpeng on 2018/8/27.
 */
@Slf4j
@RestController
@RequestMapping("subject")
public class SubjectController {

    @Autowired
    TeacherSubjectService subjectService;

    @PostMapping("")
    public Object save(@RequestBody Subject subject) {
        if (null == subject || StringUtils.isBlank(subject.getName()) || null == subject.getParent() || null == subject.getLevel()) {
            log.error("error subject info = {}", null == subject ? "null" : subject);
            throw new BizException(TeacherErrors.ILLEGAL_PARAM);
        }
        if(subject.getParent()>0){
            List<Subject> subjectList = selectByIdAndLevel(subject.getParent(),subject.getLevel()-1);
            if(CollectionUtils.isEmpty(subjectList)){
                throw new BizException(ErrorResult.create(1023111,"无效的父级ID"));
            }
        }
        subjectService.save(subject);
        return subject;
    }

    private List<Subject> selectByIdAndLevel(Long id, Integer level) {
        Example example = new Example(Subject.class);
        example.and().andEqualTo("id",id).andEqualTo("level",level);
        List<Subject> subjectList = subjectService.selectByExample(example);
        return subjectList;
    }

    @DeleteMapping("/{id}")
    public Object delete(@PathVariable Long id,@RequestParam Integer level) {
        List<Subject> subjectList = selectByParentAndLevel(id, level+1);
        if(CollectionUtils.isNotEmpty(subjectList)){
            throw new BizException(ErrorResult.create(1023111,"请删除子节点"));
        }
        Example example = new Example(Subject.class);
        example.and().andEqualTo("id",id).andEqualTo("level",level);
        return subjectService.deleteByExample(example);
    }

    private List<Subject> selectByParentAndLevel(Long parent, int level) {
        Example example = new Example(Subject.class);
        example.and().andEqualTo("parent",parent).andEqualTo("level",level);
        List<Subject> subjectList = subjectService.selectByExample(example);
        return subjectList;
    }
}
