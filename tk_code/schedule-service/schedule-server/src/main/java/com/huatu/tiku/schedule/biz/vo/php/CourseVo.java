package com.huatu.tiku.schedule.biz.vo.php;

import com.huatu.tiku.schedule.biz.domain.Course;
import com.huatu.tiku.schedule.biz.domain.CourseLive;
import com.huatu.tiku.schedule.biz.domain.CourseLiveTeacher;
import com.huatu.tiku.schedule.biz.domain.Teacher;
import com.huatu.tiku.schedule.biz.util.DateformatUtil;
import com.huatu.tiku.schedule.biz.util.TimeformatUtil;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wangjian
 **/
@Data
public class CourseVo {
    private Long id;

    private String name;

    private String examString;

    private String category;

    private List<CourseLiveVo> lives;

    public CourseVo(Course course){
        this.id=course.getId();
        this.name=course.getName();
        this.examString=null!=course.getExamType()?course.getExamType().getText():null;
        this.category=null!=course.getCourseCategory()?course.getCourseCategory().getText():null;
        List<CourseLiveVo> list=new ArrayList();
        course.getCourseLives().sort((o1,o2)->{
            int result=o1.getDateInt()-o2.getDateInt();//日期排序
            result=result==0?o1.getTimeBegin()-o2.getTimeBegin():result;//开始时间排序
            result=result==0?o1.getTimeEnd()-o2.getTimeEnd():result;//结束时间排序
            return result;
        });
        course.getCourseLives().forEach(live-> {
                list.add(new CourseLiveVo(live));
        });
        this.lives=list;
    }

    @Data
    public static class CourseLiveVo{
        private Long id;

        private Long courseId;

        private String name;

        private String Date;

        private String timeBegin;

        private String timeEnd;

        private String courseLiveCategory;

        private List<CourseLiveTeacherVo> liveTeachers;

        public CourseLiveVo(CourseLive live){
            this.id=live.getId();
            this.courseId=live.getCourseId();
            this.name=live.getName();
            this.Date=DateformatUtil.format0(live.getDate());
            this.timeBegin= TimeformatUtil.format(live.getTimeBegin());
            this.timeEnd=TimeformatUtil.format(live.getTimeEnd());
            this.courseLiveCategory= null!=live.getCourseLiveCategory()?live.getCourseLiveCategory().getText():null;
            List<CourseLiveTeacherVo> list=new ArrayList();
            live.getCourseLiveTeachers().forEach(liveTeacher->
                    list.add(new CourseLiveTeacherVo(liveTeacher))
            );
            this.liveTeachers=list;
        }

        @Data
        private class CourseLiveTeacherVo{
            private Long id;

            private Long teacherId;

            private Long scheduleTeacherId;

            private String teacherString;

            private String teacherType;

            private String subject;

            private String module;

            private CourseLiveTeacherVo(CourseLiveTeacher liveTeacher){
                this.id=liveTeacher.getId();
                Teacher teacher = liveTeacher.getTeacher();
                this.teacherId=null!=teacher?teacher.getPid():null;
                this.scheduleTeacherId=null!=teacher?teacher.getId():null;
                this.teacherString=null!=liveTeacher.getTeacher()?liveTeacher.getTeacher().getName():null;
                this.teacherType=null!=liveTeacher.getTeacherType()?liveTeacher.getTeacherType().getText():null;
                this.subject=null!=liveTeacher.getSubject()?liveTeacher.getSubject().getName():null;
            }
        }

    }
}
