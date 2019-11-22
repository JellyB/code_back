package com.huatu.tiku.schedule.biz.vo;

import com.google.common.collect.Lists;
import com.huatu.tiku.schedule.biz.domain.*;
import com.huatu.tiku.schedule.biz.enums.*;
import com.huatu.tiku.schedule.biz.util.DateformatUtil;
import com.huatu.tiku.schedule.biz.vo.CourseInfoPackage.LiveTeacherVo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.huatu.tiku.schedule.biz.util.TimeRangeUtil.intToDateString;

/**
 * 课程录播vo
 * @author wangjian
 **/
@Data
@NoArgsConstructor
public class CourseVideoVo implements Serializable {
    private static final long serialVersionUID = 4284020399982046834L;

    private Long courseId;
    private String courseName;
    private Long videoId;//直播id
    private String videoName;//直播内容
    private Long roomId;//直播间id
    private String roomName;//直播间名
    private List<LiveTeacherVo> videoTeachers;//课程直播教师名
    private String date;
    private String timeBegin;//起始时间
    private String timeEnd;//结束时间
    private List<Long> photographerIds= Lists.newArrayList();
    private List<String> photographerNames = Lists.newArrayList();

    public CourseVideoVo(CourseLive courseLive) {
        courseId=courseLive.getCourseId();
        Course course = courseLive.getCourse();
        if(null!=course&&null!=course.getName()) {
            courseName = courseLive.getCourse().getName();
        }
        videoId = courseLive.getId();
        videoName = courseLive.getName();
        date=courseLive.getDateInt().toString();
        date=DateformatUtil.format0(courseLive.getDate());
        timeBegin = intToDateString(courseLive.getTimeBegin());
        timeEnd = intToDateString(courseLive.getTimeEnd());
        VideoRoom videoRoom = courseLive.getVideoRoom();
        if (videoRoom != null) {
            roomId = videoRoom.getId();
            roomName = videoRoom.getName();
        }
        List<CourseLiveTeacher> courseLiveTeachers = courseLive.getCourseLiveTeachers();
        if (courseLiveTeachers != null && !courseLiveTeachers.isEmpty()) {
            videoTeachers = new ArrayList<>();
            for (CourseLiveTeacher courseLiveTeacher : courseLiveTeachers) {
                LiveTeacherVo liveTeacher = new LiveTeacherVo();
                liveTeacher.setLiveTeacherId(courseLiveTeacher.getId());
                CourseConfirmStatus confirm = courseLiveTeacher.getConfirm();
                if (confirm != null) {
                    liveTeacher.setConfirm(courseLiveTeacher.getConfirm());
                }
                Subject subject = courseLiveTeacher.getSubject();
                liveTeacher.setSubjectId(courseLiveTeacher.getSubjectId());
                if (subject != null) {
                    liveTeacher.setSubjectName(subject.getName());
                }
                Teacher teacher = courseLiveTeacher.getTeacher();
                liveTeacher.setTeacherId(courseLiveTeacher.getTeacherId());
                if (teacher != null) {
                    liveTeacher.setTeacherName(teacher.getName());
                }
                TeacherCourseLevel teacherCourseLevel = courseLiveTeacher.getTeacherCourseLevel();
                if (teacherCourseLevel != null) {
                    liveTeacher.setTeacherCourseLevelKey(teacherCourseLevel.getText());
                    liveTeacher.setTeacherCourseLevelValue(teacherCourseLevel.getValue());
                }
                TeacherType type = courseLiveTeacher.getTeacherType();
                if (type != null) {
                    liveTeacher.setTeacherTypeKey(type.getText());
                    liveTeacher.setTeacherTypeValue(type.getValue());
                }
                videoTeachers.add(liveTeacher);
                if(TeacherType.SYS.equals(type)){  //
                    photographerIds.add(courseLiveTeacher.getTeacherId());
                    if (teacher != null) {
                        photographerNames.add(teacher.getName());
                    }
                }
            }
        }

    }
}
