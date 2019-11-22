package com.huatu.tiku.schedule.biz.vo.CourseLivePackage;

import com.huatu.tiku.schedule.biz.domain.*;
import com.huatu.tiku.schedule.biz.enums.CourseConfirmStatus;
import com.huatu.tiku.schedule.biz.enums.CourseLiveCategory;
import com.huatu.tiku.schedule.biz.enums.CoursePhase;
import com.huatu.tiku.schedule.biz.enums.TeacherCourseLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**返回直播封装
 * @author wangjian
 **/
@Data
@NoArgsConstructor
public class CourseLiveVo implements Serializable {

    private static final long serialVersionUID = -7427316655705540288L;
    private Long liveId;//直播id
    private String liveName;//直播内容
    private Long roomId;//直播间id
    private String roomName;//直播间名
    private List<LiveTeacherVo> liveTeachers;//课程直播教师名
    private Integer timeBegin;//起始时间
    private Integer timeEnd;//结束时间
    private Long learningTeacherId;//学习师id
    private String learningTeacherName;//学习师名称
    private CourseConfirmStatus ltConfirm;//学习师是否确认
    private Long assistantId;//助教id
    private String assistantName;//助教名称
    private CourseConfirmStatus assConfirm;//助教确认标记
    private Long controllerId;//场控id
    private String controllerName;//场控名字
    private CourseConfirmStatus ctrlConfirm;//场控确认标志
    private Long compereId;//主持人id
    private String compereName;//主持人名字
    private CourseConfirmStatus comConfirm;//主持人确认标志
    private Long sourceId;//原直播id
    private Boolean sourceFlag=true;//是否原直播
    private String  courseLiveCategoryKey;//面试直播分类
    private String  courseLiveCategoryValue;

    public CourseLiveVo(CourseLive courseLive){
        liveId=courseLive.getId();
        liveName=courseLive.getName();
        timeBegin=courseLive.getTimeBegin();
        timeEnd=courseLive.getTimeEnd();
        LiveRoom liveRoom = courseLive.getLiveRoom();
        Teacher learningTeacher = courseLive.getLearningTeacher();
        if(learningTeacher!=null){
            learningTeacherId=learningTeacher.getId();
            learningTeacherName=learningTeacher.getName();
        }
        ltConfirm=courseLive.getLtConfirm();
        Teacher assistant = courseLive.getAssistant();
        if(assistant!=null){
            assistantId=assistant.getId();
            assistantName=assistant.getName();
        }
        assConfirm=courseLive.getAssConfirm();
        Teacher controller = courseLive.getController();
        if(controller!=null){
            controllerId=controller.getId();
            controllerName=controller.getName();
        }
        ctrlConfirm=courseLive.getCtrlConfirm();
        Teacher compere = courseLive.getCompere();
        if(compere!=null){
            compereId=compere.getId();
            compereName=compere.getName();
        }
        comConfirm=courseLive.getComConfirm();
        if(liveRoom!=null){
            roomId=liveRoom.getId();
            roomName=liveRoom.getName();
        }
        List<CourseLiveTeacher> courseLiveTeachers=courseLive.getCourseLiveTeachers();
        if(courseLiveTeachers!=null&&!courseLiveTeachers.isEmpty()){
            liveTeachers=new ArrayList<>();
            for(CourseLiveTeacher courseLiveTeacher:courseLiveTeachers){
                LiveTeacherVo liveTeacher=new LiveTeacherVo();
                liveTeacher.setLiveTeacherId(courseLiveTeacher.getId());
                CourseConfirmStatus confirm =courseLiveTeacher.getConfirm();
                if(confirm!=null){
                    liveTeacher.setConfirm(courseLiveTeacher.getConfirm());
                }
                CoursePhase coursePhase = courseLiveTeacher.getCoursePhase();
                if(coursePhase!=null){
                    liveTeacher.setCoursePhaseKey(coursePhase.getText());
                    liveTeacher.setCoursePhaseValue(coursePhase.getValue());
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
                Module module = courseLiveTeacher.getModule();
                if(module!=null){
                    liveTeacher.setModuleId(module.getId());
                    liveTeacher.setModuleName(module.getName());
                }
                liveTeachers.add(liveTeacher);
            }
        }

        Long sourceId = courseLive.getSourceId();//有原数据id
        if(sourceId!=null){
            this.sourceId=sourceId;
            this.sourceFlag=false;
        }
        CourseLiveCategory courseLiveCategory = courseLive.getCourseLiveCategory();
        if (courseLiveCategory!=null){
            courseLiveCategoryKey=courseLiveCategory.getText();
            courseLiveCategoryValue=courseLiveCategory.getValue();
        }
    }
}
