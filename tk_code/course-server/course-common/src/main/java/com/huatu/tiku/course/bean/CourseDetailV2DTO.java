package com.huatu.tiku.course.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author hanchao
 * @date 2017/9/13 13:15
 */
@Data
public class CourseDetailV2DTO implements Serializable{
    private static final long serialVersionUID = 1L;
    /**
     * classInfo : {"rid":"55495","Title":"2017教师资格证笔试-教育教学知识与能力（小学）","limitUserCount":0,"TimeLength":"72","ActualPrice":59,"TypeName":"系统精讲班","startTime":0,"stopTime":0,"scaleimg":"http://upload.htexam.net/classimg/class/1482392281.jpg","studyDate":"1月7日-3月31日","TeacherName":"高倩倩，孟贞贞"}
     */

    private ClassInfo classInfo;
    private ClassInfo teacher_informatioin;//为了适配老接口上用的变量名

    @Data
    public static class ClassInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        /**
         * rid : 55495
         * Title : 2017教师资格证笔试-教育教学知识与能力（小学）
         * limitUserCount : 0
         * TimeLength : 72
         * ActualPrice : 59
         * TypeName : 系统精讲班
         * startTime : 0
         * stopTime : 0
         * scaleimg : http://upload.htexam.net/classimg/class/1482392281.jpg
         * studyDate : 1月7日-3月31日
         * TeacherName : 高倩倩，孟贞贞
         */

        private String rid;
        @JsonProperty
        private String Title;
        private Integer limitUserCount;
        @JsonProperty
        private String TimeLength;
        @JsonProperty
        private Integer ActualPrice;
        @JsonProperty
        private String TypeName;
        private String startTime;
        private String stopTime;
        private String scaleimg;
        private String studyDate;
        @JsonProperty
        private String TeacherName;
        private int isBuy;
        private int total;
        private int limitTimes;
        private int limitStatus;
    }
}
