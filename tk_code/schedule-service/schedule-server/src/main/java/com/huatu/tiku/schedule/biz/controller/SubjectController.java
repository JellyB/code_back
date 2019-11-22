package com.huatu.tiku.schedule.biz.controller;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.huatu.tiku.schedule.base.config.CustomUser;
import com.huatu.tiku.schedule.biz.domain.Role;
import com.huatu.tiku.schedule.biz.domain.Subject;
import com.huatu.tiku.schedule.biz.enums.ExamType;
import com.huatu.tiku.schedule.biz.service.SubjectService;
import com.huatu.tiku.schedule.biz.vo.SubjectVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * 科目Controller
 *
 * @author Geek-S
 */
@RestController
@RequestMapping("subject")
public class SubjectController {

    private final SubjectService subjectService;

    @Autowired
    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    /**
     * 根据考试类型查询科目
     *
     * @param examType 考试类型
     * @return 科目列表
     */
    @GetMapping("findByExamType")
    public List<SubjectVo> findByExamType(ExamType examType) {

        List<Subject> subjects = subjectService.findByExamType(examType);
        subjects.sort((o1,o2)-> o2.getSort()-o1.getSort());
        List<SubjectVo> subjectVos = Lists.newArrayList();
        subjects.forEach(subject -> {
            SubjectVo subjectVo = new SubjectVo(subject);
            subjectVos.add(subjectVo);
        });
        return subjectVos;
    }

    /**
     * 通过类型和角色获取科目
     */
    @GetMapping("findByExamTypeAndRole")
    public List<SubjectVo> findByExamTypeAndRole(ExamType examType,@AuthenticationPrincipal CustomUser user) {
        List<Subject> subjects = subjectService.findByExamType(examType);
        subjects.sort((o1,o2)-> o2.getSort()-o1.getSort());
        List<SubjectVo> subjectVos =  Lists.newArrayList();
        List<String> roleNames = Lists.newArrayList("超级管理员", "人力","教学管理组");
        Set<Role> roles = user.getRoles();
        Optional<Role> adminFlag = roles.stream().filter(role -> roleNames.contains(role.getName())).findFirst();
        if (!adminFlag.isPresent()) { //不是指定角色 判断权限
            Optional<Role> jwFlag = roles.stream().filter(role -> role.getName().equals("教务")).findFirst();
            if (jwFlag.isPresent()) { // 教务
                subjects.forEach(subject -> {
                    SubjectVo subjectVo = new SubjectVo(subject);
                    subjectVos.add(subjectVo);
                });
            }else{  //不是教务
                if(user.getSubjectId()!=null){
                    Subject subject = subjectService.findOne(user.getSubjectId());
                    SubjectVo subjectVo = new SubjectVo(subject);
                    subjectVos.add(subjectVo);
                }
            }
        }else{
            subjects.forEach(subject -> {
                SubjectVo subjectVo = new SubjectVo(subject);
                subjectVos.add(subjectVo);
            });
        }
        return subjectVos;
    }

    /**
     * 根据考试类型和科目查询科目
     *
     * @param examType 考试类型
     * @return 科目列表
     */
    @GetMapping("findByExamTypeAndSubjectId")
    public List<SubjectVo> findByExamTypeAndSubjectId(ExamType examType, Long subjectId) {
        List<Subject> subjects = subjectService.findByExamType(examType, subjectId);
        subjects.sort((o1,o2)-> o2.getSort()-o1.getSort());
        List<SubjectVo> subjectVos = new ArrayList();
        subjects.forEach(subject -> {
            SubjectVo subjectVo = new SubjectVo(subject);
            subjectVos.add(subjectVo);
        });
        return subjectVos;
    }

    /**
     * 根据角色获得固定组合
     */
    @GetMapping("groupSubjectIds")
    public List<Map<String, Object>> groupSubjectIds(@AuthenticationPrincipal CustomUser user) {
        List<Map<String, Object>> result = Lists.newArrayList();
        List<String> roleNames = Lists.newArrayList("超级管理员", "人力","教学管理组");
        Set<Role> roles = user.getRoles();
        Optional<Role> adminFlag = roles.stream().filter(role -> roleNames.contains(role.getName())).findFirst();
        if (!adminFlag.isPresent()) { //不是指定角色 判断权限
            Optional<Role> jwFlag = roles.stream().filter(role -> role.getName().equals("教务")||role.getName().equals("运营")||
                    role.getName().equals("录播产品")||role.getName().equals("录播教务")).findFirst();
            if (jwFlag.isPresent()) { // 教务或运营
                Set<ExamType> dataPermissioins = user.getDataPermissions();
                if(dataPermissioins.contains(ExamType.GWY)){
                    result.add(ImmutableMap.of("value", "公职笔试行测+招警", "text", Lists.newArrayList(1l,2l,3l,4l,7l,33l)));
                }
                if(dataPermissioins.contains(ExamType.GWY)&&dataPermissioins.contains(ExamType.LX)){
                    result.add(ImmutableMap.of("value", "公职申论+遴选", "text", Lists.newArrayList(5l,47l)));
                }
                if(dataPermissioins.contains(ExamType.SYDW)&&dataPermissioins.contains(ExamType.YLWS)){
                    result.add(ImmutableMap.of("value", "事业单位+医疗", "text", Lists.newArrayList(65l,70l,71l,72l,73l,34l,35l)));
                }
            }
        }else{  //指定角色 返回全部类型
            result.add(ImmutableMap.of("value", "公职笔试行测+招警", "text", Lists.newArrayList(1l,2l,3l,4l,7l,33l)));
            result.add(ImmutableMap.of("value", "公职申论+遴选", "text", Lists.newArrayList(5l,47l)));
            result.add(ImmutableMap.of("value", "事业单位+医疗", "text", Lists.newArrayList(65l,70l,71l,72l,73l,34l,35l)));
        }
        return result;
    }


}
