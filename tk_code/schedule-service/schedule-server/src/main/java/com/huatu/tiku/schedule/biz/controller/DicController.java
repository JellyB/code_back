package com.huatu.tiku.schedule.biz.controller;

import java.time.LocalDate;
import java.util.*;

import com.huatu.tiku.schedule.base.config.CustomUser;
import com.huatu.tiku.schedule.biz.domain.Role;
import com.huatu.tiku.schedule.biz.enums.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * 字典Controller
 *
 * @author Geek-S
 */
@RestController
@RequestMapping("dic")
public class DicController {

    /**
     * 获取考试类型字典
     *
     * @return 考试类型字典
     */
    @GetMapping("examType")
    public List<Map<String, String>> examType() {
        List<Map<String, String>> examTypeDic = Lists.newArrayList();

        for (ExamType examType : ExamType.values()) {
            if (examType.getStatus())
                examTypeDic.add(ImmutableMap.of("value", examType.name(), "text", examType.getText()));
        }

        return examTypeDic;
    }

    /** 规则界面专用 添加全部类型
     * 获取考试类型字典
     *
     * @return 考试类型字典
     */
    @GetMapping("ruleExamType")
    public List<Map<String, String>> ruleExamType() {
        List<Map<String, String>> examTypeDic = Lists.newArrayList();
        examTypeDic.add(ImmutableMap.of("value", ExamType.ALL.name(), "text",  ExamType.ALL.getText()));
        for (ExamType examType : ExamType.values()) {
            if (examType.getStatus()) {
                examTypeDic.add(ImmutableMap.of("value", examType.name(), "text", examType.getText()));
            }
        }
        return examTypeDic;
    }

    /**根据权限返回类型
     * 获取创建课程考试类型
     * @return 考试类型字典
     */
    @GetMapping("scheduleExamType")
    public List<Map<String, String>> scheduleExamType(@AuthenticationPrincipal CustomUser user) {
        List<Map<String, String>> examTypeDic = Lists.newArrayList();

        List<String> roleNames = Lists.newArrayList("超级管理员", "人力","教学管理组");
        Set<Role> roles = user.getRoles();
        Optional<Role> adminFlag = roles.stream().filter(role -> roleNames.contains(role.getName())).findFirst();
        if (!adminFlag.isPresent()) { //不是指定角色 判断权限
            Optional<Role> jwFlag = roles.stream().filter(role -> role.getName().equals("教务")||role.getName().equals("运营")||
                    role.getName().equals("录播产品")||role.getName().equals("录播教务")).findFirst();
            if (jwFlag.isPresent()) { // 教务或运营
                Set<ExamType> dataPermissioins = user.getDataPermissions();
                List<ExamType> list=new ArrayList();
                list.addAll(dataPermissioins);
                Collections.sort(list, Comparator.comparingInt(ExamType::getId));//排序
                for (ExamType examType :list) {
                    if (examType.getStatus())
                        examTypeDic.add(ImmutableMap.of("value", examType.name(), "text", examType.getText()));
                }
            }
        }else{  //指定角色 返回全部类型
            for (ExamType examType : ExamType.values()) {
                if (examType.getStatus())
                    examTypeDic.add(ImmutableMap.of("value", examType.name(), "text", examType.getText()));
            }
        }
        return examTypeDic;
    }

    /**课表课时界面
     * 根据角色获取类型 非角色时取出自己所属类型
     */
    @GetMapping("examTypeByRole")
    public List<Map<String, String>> examTypeByRole(@AuthenticationPrincipal CustomUser user) {
        List<Map<String, String>> examTypeDic = Lists.newArrayList();

        List<String> roleNames = Lists.newArrayList("超级管理员", "人力","教学管理组");
        Set<Role> roles = user.getRoles();
        Optional<Role> adminFlag = roles.stream().filter(role -> roleNames.contains(role.getName())).findFirst();
        if (!adminFlag.isPresent()) { //不是指定角色 判断权限
            Optional<Role> jwFlag = roles.stream().filter(role -> role.getName().equals("教务")||role.getName().equals("运营")||
                    role.getName().equals("录播产品")||role.getName().equals("录播教务")).findFirst();
            if (jwFlag.isPresent()) { // 教务
                Set<ExamType> dataPermissioins = user.getDataPermissions();
                List<ExamType> list=new ArrayList();
                list.addAll(dataPermissioins);
                Collections.sort(list, Comparator.comparingInt(ExamType::getId));//排序
                for (ExamType examType :list) {
                    if (examType.getStatus())
                        examTypeDic.add(ImmutableMap.of("value", examType.name(), "text", examType.getText()));
                }
            }else{
                if (user.getExamType()!=null) {
                    examTypeDic.add(ImmutableMap.of("value", user.getExamType().name(), "text", user.getExamType().getText()));
                }
            }
        }else{  //指定角色 返回全部类型
            for (ExamType examType : ExamType.values()) {
                if (examType.getStatus())
                    examTypeDic.add(ImmutableMap.of("value", examType.name(), "text", examType.getText()));
            }
        }
        return examTypeDic;
    }

    /**
     * 获取教师等级
     *
     * @return 教师等级字典
     */
    @GetMapping("teacherLevel")
    public List<Map<String, String>> teacherLevel() {
        List<Map<String, String>> teacherLevelDic = Lists.newArrayList();

        for (TeacherLevel teacherLevel : TeacherLevel.values()) {
            teacherLevelDic.add(ImmutableMap.of("value", teacherLevel.name(), "text", teacherLevel.getText()));
        }

        return teacherLevelDic;
    }

    /**
     * 获取教师授课等级
     *
     * @return 教师授课等级字典
     */
    @GetMapping("teacherCourseLevel")
    public List<Map<String, String>> teacherCourseLevel() {
        List<Map<String, String>> teacherCourseLevelDic = Lists.newArrayList();

        for (TeacherCourseLevel teacherCourseLevel : TeacherCourseLevel.values()) {
            teacherCourseLevelDic
                    .add(ImmutableMap.of("value", teacherCourseLevel.name(), "text", teacherCourseLevel.getText()));
        }
        Map<String, String> stringStringMap = teacherCourseLevelDic.get(0);
        teacherCourseLevelDic.remove(0);
        teacherCourseLevelDic.add(stringStringMap);
        return teacherCourseLevelDic;
    }

    /**
     * 获取课程类型
     *
     * @return 课程类型字典
     */
    @GetMapping("courseCategory")
    public List<Map<String, String>> courseCategory() {
        List<Map<String, String>> courseCategoryDic = Lists.newArrayList();

        for (CourseCategory courseCategory : CourseCategory.values()) {
            courseCategoryDic.add(ImmutableMap.of("value", courseCategory.name(), "text", courseCategory.getText()));
        }

        return courseCategoryDic;
    }

    /**
     * 获取课程类型
     *
     * @return 课程类型字典
     */
    @GetMapping("courseCategoryByRole")
    public List<Map<String, String>> courseCategoryByRole(@AuthenticationPrincipal CustomUser user) {
        List<Map<String, String>> courseCategoryDic = Lists.newArrayList();
        List<String> roleNames = Lists.newArrayList("超级管理员", "人力","教学管理组");
        // 当前用户角色
        Set<Role> roles = user.getRoles();
        // 管理员
        Optional<Role> adminFlag = roles.stream().filter(role -> roleNames.contains(role.getName())).findFirst();
        if(adminFlag.isPresent()){
            courseCategoryDic.add(ImmutableMap.of("value", CourseCategory.LIVE.name(), "text", CourseCategory.LIVE.getText()));
            courseCategoryDic.add(ImmutableMap.of("value", CourseCategory.XXK.name(), "text", CourseCategory.XXK.getText()));
            courseCategoryDic.add(ImmutableMap.of("value", CourseCategory.VIDEO.name(), "text", CourseCategory.VIDEO.getText()));
            courseCategoryDic.add(ImmutableMap.of("value", CourseCategory.SSK.name(), "text", CourseCategory.SSK.getText()));
            courseCategoryDic.add(ImmutableMap.of("value", CourseCategory.DMJZ.name(), "text", CourseCategory.DMJZ.getText()));
            return courseCategoryDic;
        }
        Optional<Role> lbFlag = roles.stream().filter(role -> role.getName().equals("录播产品")|| role.getName().equals("录播教务")).findFirst();
        if (lbFlag.isPresent()) { //不是指定角色 判断权限
            courseCategoryDic.add(ImmutableMap.of("value", CourseCategory.VIDEO.name(), "text", CourseCategory.VIDEO.getText()));
        }
        Optional<Role> yyFlag = roles.stream().filter(role -> role.getName().equals("教务")||role.getName().equals("运营")).findFirst();
        if (yyFlag.isPresent()) { //不是指定角色 判断权限
            courseCategoryDic.add(ImmutableMap.of("value", CourseCategory.LIVE.name(), "text", CourseCategory.LIVE.getText()));
            courseCategoryDic.add(ImmutableMap.of("value", CourseCategory.XXK.name(), "text", CourseCategory.XXK.getText()));
            courseCategoryDic.add(ImmutableMap.of("value", CourseCategory.SSK.name(), "text", CourseCategory.SSK.getText()));
            courseCategoryDic.add(ImmutableMap.of("value", CourseCategory.DMJZ.name(), "text", CourseCategory.DMJZ.getText()));
        }

        return courseCategoryDic;
    }

    /**
     * 取得教师类型
     *
     * @return 教师类型字典
     */
    @GetMapping("teacherType")
    public List<Map<String, String>> teacherType() {
        List<Map<String, String>> teacherTypeDic = Lists.newArrayList();

        for (TeacherType teacherType : TeacherType.values()) {
            teacherTypeDic.add(ImmutableMap.of("value", teacherType.name(), "text", teacherType.getText()));
        }
        teacherTypeDic.remove(2);//去除学习师
        return teacherTypeDic;
    }

    /**
     * 教师审核状态
     *
     * @return 审核状态字典
     */
    @GetMapping("teacherStatus")
    public List<Map<String, String>> TeacherStatus() {
        List<Map<String, String>> teacherStatusDic = Lists.newArrayList();

        for (TeacherStatus teacherStatus : TeacherStatus.values()) {
            teacherStatusDic.add(ImmutableMap.of("value", teacherStatus.name(), "text", teacherStatus.getText()));
        }

        return teacherStatusDic;
    }

    /**
     * 直播教师状态
     *
     * @return 直播教师状态字典
     */
    @GetMapping("courseStatus")
    public List<Map<String, String>> CourseStatus() {
        List<Map<String, String>> courseStatusDic = Lists.newArrayList();

        for (CourseStatus courseStatus : CourseStatus.values()) {
            courseStatusDic.add(ImmutableMap.of("value", courseStatus.name(), "text", courseStatus.getText()));
        }

        return courseStatusDic;
    }

    /**
     * 获取年份
     *
     * @return 年份列表
     */
    @GetMapping("years")
    public List<Map<String, Object>> years(String suffix) {
        List<Map<String, Object>> years = Lists.newArrayList();

        int start = 2018;

        int end = LocalDate.now().getYear();

        if (suffix == null) {
            suffix = "";
        }

        for (; start <= end; start++) {

            years.add(ImmutableMap.of("value", start, "text", start + suffix));
        }

        return years;
    }

    private final String[] NUMBERS = new String[]{"一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "十二"};

    /**
     * 获取月份
     *
     * @return 月份列表
     */
    @GetMapping("months")
    public List<Map<String, Object>> months(Integer year, Integer type, String suffix) {
        List<Map<String, Object>> months = Lists.newArrayList();

        int start = 1;

        LocalDate now = LocalDate.now();

        int yearCurrent = now.getYear();

        int end;

        if (year < yearCurrent) {
            end = 12;
        } else if (year == yearCurrent) {
            end = now.getMonthValue();
        } else {
            end = 0;
        }

        if (suffix == null) {
            suffix = "";
        }

        for (; start <= end; start++) {
            if (type != null && type == 1) {
                months.add(ImmutableMap.of("value", start, "text", NUMBERS[start - 1] + suffix));
            } else {
                months.add(ImmutableMap.of("value", start, "text", start + suffix));
            }
        }

        return months;
    }

    /**
     * 面试直播分类
     *
     * @return 面试直播分类字典
     */
    @GetMapping("courseLiveCategory")
    public List<Map<String, String>> courseLiveCategory() {
        List<Map<String, String>> courseLiveCategoryDic = Lists.newArrayList();

        for (CourseLiveCategory courseLiveCategory : CourseLiveCategory.values()) {
            courseLiveCategoryDic.add(ImmutableMap.of("value", courseLiveCategory.name(), "text", courseLiveCategory.getText()));
        }

        return courseLiveCategoryDic;
    }

    /**
     * 反馈状态
     *
     * @return 面试直播分类字典
     */
    @GetMapping("feedbackStatus")
    public List<Map<String, String>> feedbackStatus() {
        List<Map<String, String>> feedbackStatusDic = Lists.newArrayList();

        for (FeedbackStatus feedbackStatus : FeedbackStatus.values()) {
            feedbackStatusDic.add(ImmutableMap.of("value", feedbackStatus.name(), "text", feedbackStatus.getText()));
        }

        return feedbackStatusDic;
    }

    @GetMapping("schoolType")
    public List<Map<String, String>> schoolType() {
        List<Map<String, String>> schoolTypes = Lists.newArrayList();

        for (SchoolType schoolType : SchoolType.values()) {
            schoolTypes.add(ImmutableMap.of("value", schoolType.name(), "text", schoolType.getText()));
        }

        return schoolTypes;
    }
}
