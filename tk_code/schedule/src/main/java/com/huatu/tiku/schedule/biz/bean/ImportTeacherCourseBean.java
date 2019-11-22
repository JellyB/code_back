package com.huatu.tiku.schedule.biz.bean;

import com.huatu.tiku.schedule.biz.enums.CoursePhase;
import lombok.Data;

import java.util.Date;

/**
 * Created by duanxiangchao on 2018/5/11
 */
@Data
public class ImportTeacherCourseBean {

    /**
     * 直播id
     */
    private Long courseLiveId;

    private Long sourceId;

    private Date date;

    private Integer timeBegin;

    private Integer timeEnd;

    private String courseName;

    private CoursePhase coursePhase;

    private String teacherName;

    private boolean isRoll = false;
    /**
     * 滚动排课 源课程
     */
    private ImportTeacherCourseBean teacherCourseBean;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"courseLiveId\":")
                .append(courseLiveId);
        sb.append(",\"sourceId\":")
                .append(sourceId);
        sb.append(",\"date\":\"")
                .append(date).append('\"');
        sb.append(",\"timeBegin\":")
                .append(timeBegin);
        sb.append(",\"timeEnd\":")
                .append(timeEnd);
        sb.append(",\"courseName\":\"")
                .append(courseName).append('\"');
        sb.append(",\"coursePhase\":")
                .append(coursePhase);
        sb.append(",\"teacherName\":\"")
                .append(teacherName).append('\"');
        sb.append(",\"isRoll\":")
                .append(isRoll);
        sb.append(",\"teacherCourseBean\":")
                .append(teacherCourseBean);
        sb.append('}');
        return sb.toString();
    }
}
