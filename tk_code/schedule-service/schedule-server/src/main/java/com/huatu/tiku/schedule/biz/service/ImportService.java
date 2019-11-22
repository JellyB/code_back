package com.huatu.tiku.schedule.biz.service;

import com.huatu.tiku.schedule.biz.dto.php.PHPUpdateTeacherDto;
import com.huatu.tiku.schedule.biz.enums.CourseCategory;
import com.huatu.tiku.schedule.biz.enums.ExamType;

import java.util.List;

/**
 * @author wangjian
 **/
public interface ImportService {

    void importTeacherCourse(List<List<List<String>>> importList, Long courseId, Boolean flag);

    void importCourseRoll(List<List<List<String>>> importList);

    void importCourse(List<List<List<String>>> importList, String courseName, ExamType examType, CourseCategory category);

    void importCourseRoll(List<List<List<String>>> list, ExamType examType, CourseCategory category);

    /**
     * 导入教师
     * @param list 教师数据
     */
    void importTeachers(List<List<List<String>>> list);

    /**
     * 从php批量导入数据
     * @param data teachers
     */
    void importTeacherByPHP(List<PHPUpdateTeacherDto> data);
}
