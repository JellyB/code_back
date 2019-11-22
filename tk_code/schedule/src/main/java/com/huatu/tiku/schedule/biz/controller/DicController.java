package com.huatu.tiku.schedule.biz.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.huatu.tiku.schedule.biz.enums.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * 字典Controller
 * 
 * @author Geek-S
 *
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
			if(examType.getStatus())
			examTypeDic.add(ImmutableMap.of("value", examType.name(), "text", examType.getText()));
		}

		return examTypeDic;
	}

	/**
	 * 获取课程阶段字典
	 * 
	 * @return 课程阶段字典
	 */
	@GetMapping("coursePhase")
	public List<Map<String, String>> coursePhase() {
		List<Map<String, String>> coursePhaseDic = Lists.newArrayList();

		for (CoursePhase coursePhase : CoursePhase.values()) {
			coursePhaseDic.add(ImmutableMap.of("value", coursePhase.name(), "text", coursePhase.getText()));
		}

		return coursePhaseDic;
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
	 * 取得教师类型
	 * @return 教师类型字典
	 */
	@GetMapping("teacherType")
	public List<Map<String, String>> teacherType() {
		List<Map<String, String>> teacherTypeDic = Lists.newArrayList();

		for (TeacherType teacherType : TeacherType.values()) {
			teacherTypeDic.add(ImmutableMap.of("value", teacherType.name(), "text", teacherType.getText()));
		}

		return teacherTypeDic;
	}

    /**
     * 教师审核状态
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

	private final String[] NUMBERS = new String[] { "一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "十二" };

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
	 * @return 面试直播分类字典
	 */
	@GetMapping("courseLiveCategory")
	public List<Map<String, String>> CourseLiveCategory() {
		List<Map<String, String>> courseLiveCategoryDic = Lists.newArrayList();

		for (CourseLiveCategory courseLiveCategory : CourseLiveCategory.values()) {
			courseLiveCategoryDic.add(ImmutableMap.of("value", courseLiveCategory.name(), "text", courseLiveCategory.getText()));
		}

		return courseLiveCategoryDic;
	}
}
