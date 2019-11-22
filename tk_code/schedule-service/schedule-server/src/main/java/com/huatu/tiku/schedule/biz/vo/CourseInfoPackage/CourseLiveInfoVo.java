package com.huatu.tiku.schedule.biz.vo.CourseInfoPackage;

import com.google.common.collect.Lists;
import com.huatu.tiku.schedule.biz.domain.*;
import com.huatu.tiku.schedule.biz.enums.*;
import com.huatu.tiku.schedule.biz.util.DateformatUtil;
import com.huatu.tiku.schedule.biz.util.TimeRangeUtil;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author wangjian
 **/
@Data
class CourseLiveInfoVo implements Serializable{

    private static final long serialVersionUID = 2711671636456429243L;

    private Long id;
    private Long courseId;//课程id
    private String name;//课程名
    private String dateString;//日期
    private Integer dateInt;//日期
    private Date date;//日期
//    private String time;//时间
    private String[] time;
    private Integer timeBegin;//开始时间
    private Integer timeEnd;//结束时间
    private String videoRoomName;//录播间
    private Long videoRoomId;//录播间
    private Long sourceId;//源id
    private CourseLiveCategory courseLiveCategory;//授课类型
    private List<LiveTeacherVo> liveTeachers;//课程直播教师名
    private LiveTeacherVo controller;//场控
    private LiveTeacherVo compere;//主持人

    CourseLiveInfoVo(CourseLive courseLive){
        this.id=courseLive.getId();
        this.courseId=courseLive.getCourseId();
        this.name=courseLive.getName();
        this.date=courseLive.getDate();
        this.dateString= DateformatUtil.format4(this.date);
        this.dateInt=courseLive.getDateInt();
        this.timeBegin=courseLive.getTimeBegin();
        this.timeEnd=courseLive.getTimeEnd();
//        this.time= TimeRangeUtil.intToDateString(timeBegin)+"~"+TimeRangeUtil.intToDateString(timeEnd);
        time=new String[2];
        time[0]= DateformatUtil.format0(this.date)+" "+TimeRangeUtil.intToDateString(timeBegin)+":00";
        time[1]= DateformatUtil.format0(this.date)+" "+TimeRangeUtil.intToDateString(timeEnd)+":00";
        this.videoRoomId=courseLive.getVideoRoomId();
        Long videoRoomId = courseLive.getVideoRoomId();
        if(videoRoomId!=null){
            VideoRoom videoRoom = courseLive.getVideoRoom();
            this.videoRoomName=videoRoom.getName();
        }
        this.sourceId=courseLive.getSourceId();
        this.courseLiveCategory=courseLive.getCourseLiveCategory();

        List<CourseLiveTeacher> courseLiveTeachers=courseLive.getCourseLiveTeachers();
        if(courseLiveTeachers!=null&&!courseLiveTeachers.isEmpty()){
            liveTeachers= Lists.newArrayList();
            for(CourseLiveTeacher courseLiveTeacher:courseLiveTeachers){
                LiveTeacherVo liveTeacher=new LiveTeacherVo();
                liveTeacher.setLiveTeacherId(courseLiveTeacher.getId());
                CourseConfirmStatus confirm =courseLiveTeacher.getConfirm();
                if(confirm!=null){
                    liveTeacher.setConfirm(courseLiveTeacher.getConfirm());
                }
                Subject subject = courseLiveTeacher.getSubject();
                if(subject!=null){
                    liveTeacher.setSubjectId(subject.getId());
                    liveTeacher.setSubjectName(subject.getName());
                }
                Teacher teacher = courseLiveTeacher.getTeacher();
                if(teacher!=null){
                    liveTeacher.setTeacherId(teacher.getId());
                    liveTeacher.setTeacherName(teacher.getName());
                }
                TeacherCourseLevel teacherCourseLevel = courseLiveTeacher.getTeacherCourseLevel();
                if(teacherCourseLevel!=null){
                    liveTeacher.setTeacherCourseLevelKey(teacherCourseLevel.getText());
                    liveTeacher.setTeacherCourseLevelValue(teacherCourseLevel.getValue());
                }
                TeacherType type = courseLiveTeacher.getTeacherType();
                if(type!=null){
                    liveTeacher.setTeacherTypeKey(type.getText());
                    liveTeacher.setTeacherTypeValue(type.getValue());
                    if(TeacherType.CK.equals(type)){
                        controller=liveTeacher;
                    }
                    if(TeacherType.ZCR.equals(type)){
                        compere=liveTeacher;
                    }
                }
                liveTeachers.add(liveTeacher);
            }
        }
    }
}
