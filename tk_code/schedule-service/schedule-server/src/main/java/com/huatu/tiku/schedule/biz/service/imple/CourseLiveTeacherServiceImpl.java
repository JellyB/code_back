package com.huatu.tiku.schedule.biz.service.imple;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.huatu.tiku.schedule.base.exception.BadRequestException;
import com.huatu.tiku.schedule.biz.domain.Teacher;
import com.huatu.tiku.schedule.biz.enums.*;
import com.huatu.tiku.schedule.biz.repository.CourseRepository;
import com.huatu.tiku.schedule.biz.util.SmsUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.huatu.tiku.schedule.base.service.impl.BaseServiceImpl;
import com.huatu.tiku.schedule.biz.domain.CourseLive;
import com.huatu.tiku.schedule.biz.domain.CourseLiveTeacher;
import com.huatu.tiku.schedule.biz.dto.SaveCourseLiveTeacherBatchDto;
import com.huatu.tiku.schedule.biz.repository.CourseLiveRepository;
import com.huatu.tiku.schedule.biz.repository.CourseLiveTeacherRepository;
import com.huatu.tiku.schedule.biz.service.CourseLiveTeacherService;

@Slf4j
@Service
public class CourseLiveTeacherServiceImpl extends BaseServiceImpl<CourseLiveTeacher, Long>
        implements CourseLiveTeacherService {

    private final CourseLiveTeacherRepository courseLiveTeacherRepository;

    private final CourseLiveRepository courseLiveRepository;

    private final CourseRepository courseRepository;

    @Autowired
    public CourseLiveTeacherServiceImpl(CourseLiveTeacherRepository courseLiveTeacherRepository,
                                        CourseLiveRepository courseLiveRepository, CourseRepository courseRepository) {
        this.courseLiveTeacherRepository = courseLiveTeacherRepository;
        this.courseLiveRepository = courseLiveRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    public int bindTeacher(Long courseLiveTeacherId, Long teacherId) {
        return courseLiveTeacherRepository.bindTeacher(courseLiveTeacherId, teacherId,CourseConfirmStatus.DQR.ordinal());
    }

    @Override
    @Transactional
    public List<CourseLiveTeacher> saveCourseLiveTeacherBatch(SaveCourseLiveTeacherBatchDto courseLiveTeacherBatchDto, boolean isCover) {
        List<CourseLiveTeacher> courseLiveTeachers = Lists.newArrayList();
        List<Long> courseLiveIds = courseLiveTeacherBatchDto.getCourseLiveIds();
        for (Long courseLiveId : courseLiveIds) {
            CourseLive courseLive = courseLiveRepository.findOne(courseLiveId);
            Long sourceId = courseLive.getSourceId();
            if(null!=sourceId){
                continue;
            }
            List<CourseLiveTeacher> courseLiveTeachersList = courseLive.getCourseLiveTeachers();
            for (CourseLiveTeacher next : courseLiveTeachersList) {
                if (isCover) {  //覆盖时 将原有数据删除
                    courseLiveTeacherRepository.deleteById(next.getId());
                } else {  //非覆盖 清空全空数据
                    if (next.getTeacherCourseLevel() == null && next.getSubjectId() == null) {
                        courseLiveTeacherRepository.deleteById(next.getId());
                    }
                }
            }

            // 创建直播教师
            CourseLiveTeacher courseLiveTeacher = new CourseLiveTeacher();
            courseLiveTeacher.setCourseLiveId(courseLiveId);
            courseLiveTeacher.setTeacherCourseLevel(courseLiveTeacherBatchDto.getTeacherCourseLevel());
            courseLiveTeacher.setSubjectId(courseLiveTeacherBatchDto.getSubjectId());
            courseLiveTeacher.setConfirm(CourseConfirmStatus.DQR);//初始状态
            courseLiveTeacher.setTeacherType(TeacherType.JS);//教师类型
            courseLiveTeacher=courseLiveTeacherRepository.save(courseLiveTeacher);

            // 返回课程直播教师
            courseLiveTeachers.add(courseLiveTeacher);
            //滚动内容处理
            List<CourseLive> rollLive = courseLiveRepository.findSourceId(courseLiveId);
            for (CourseLive live : rollLive) {
                List<CourseLiveTeacher> rollLiveTeachersList = live.getCourseLiveTeachers();
                for (CourseLiveTeacher next : rollLiveTeachersList) {
                    if (isCover) {  //覆盖时 将原有数据删除
                        courseLiveTeacherRepository.deleteById(next.getId());
                    } else {  //非覆盖 清空全空数据
                        if (next.getTeacherCourseLevel() == null && next.getSubjectId() == null) {
                            courseLiveTeacherRepository.deleteById(next.getId());
                        }
                    }
                }

                // 创建直播教师
                CourseLiveTeacher rollLiveTeacher = new CourseLiveTeacher();
                rollLiveTeacher.setCourseLiveId(live.getId());
                rollLiveTeacher.setTeacherCourseLevel(courseLiveTeacherBatchDto.getTeacherCourseLevel());
                rollLiveTeacher.setSubjectId(courseLiveTeacherBatchDto.getSubjectId());
                rollLiveTeacher.setConfirm(CourseConfirmStatus.DQR);//初始状态
                rollLiveTeacher.setTeacherType(TeacherType.JS);//教师类型
                rollLiveTeacher.setSourceId(courseLiveTeacher.getId());
                courseLiveTeacherRepository.save(rollLiveTeacher);
            }
        }
        return courseLiveTeachers;
    }

    @Override
    public int updateTaskTeacher(Long teacherId, List<Long> ids, CourseConfirmStatus courseConfirmStatus) {
        List<BigInteger> rollIds = courseLiveTeacherRepository.findByTeacherIdAndsAndSourceLiveId(teacherId, ids);
        if(!rollIds.isEmpty()){
            return courseLiveTeacherRepository.updateTaskTeacher(teacherId, ids, courseConfirmStatus,rollIds.stream().map(BigInteger::longValue).collect(Collectors.toList()));
        }
        return courseLiveTeacherRepository.updateTaskTeacher(teacherId, ids, courseConfirmStatus);
    }

    @Override
    public int savaSubject(Long courseLiveTeacherId, Long subjectId) {
        return courseLiveTeacherRepository.savaSubject(courseLiveTeacherId, subjectId);
    }

    @Override
    public int savaLiveTeacherLevel(Long courseLiveTeacherId, TeacherCourseLevel teacherCourseLevel) {
        return courseLiveTeacherRepository.savaLiveTeacherLevel(courseLiveTeacherId, teacherCourseLevel);
    }

    @Override
    public void findCourseStatusByCourseLiveTeacherId(Long courseLiveTeacherId) {
        CourseStatus status = courseLiveTeacherRepository.findCourseStatusByCourseLiveTeacherId(courseLiveTeacherId);
        if (status != null && status.equals(CourseStatus.ZBAP)) {//如果直播安排状态 表示已经被运营撤销
            throw new BadRequestException("该课程已撤销,请勿操作");
        }
    }

    @Override
    public CourseLiveTeacher findByCourseLiveIdAndTeacherId(Long courseLiveId, Long teacherId) {
        return courseLiveTeacherRepository.findByCourseLiveIdAndTeacherId(courseLiveId, teacherId);
    }

    @Override
    public void sendSmsToAbout(Long liveId, Long teacherId) {
        CourseLive live = courseLiveRepository.findOne(liveId);
        List<CourseLiveTeacher> courseLiveTeachers = live.getCourseLiveTeachers();
        Iterator<CourseLiveTeacher> iterator = courseLiveTeachers.iterator();//迭代器
        Teacher oldTeacher=null;
        Teacher newTeacher=null;
        TeacherType type=null;
        CourseLiveTeacher oldData=null;
        while(iterator.hasNext()){
            CourseLiveTeacher next = iterator.next();
            if(teacherId.equals(next.getTeacherId())&&null!=next.getLastTeacherId()&&!teacherId.equals(next.getLastTeacherId())){  //本教师是代替的另一个教师时
                oldData=next;
                oldTeacher = next.getLastTeacher();//原本的教师
                newTeacher = next.getTeacher();//新教师
                type=next.getTeacherType();
                iterator.remove();//移除此数据
                break;//结束迭代
            }
        }
        if(null!=oldTeacher&&null!=newTeacher){
            iterator=courseLiveTeachers.iterator();//迭代器
            while(iterator.hasNext()){
                CourseLiveTeacher next = iterator.next();
                if(CourseConfirmStatus.QR.equals(next.getConfirm())){//给确认状态的相关教师发送通知短信
                    Teacher teacher = next.getTeacher();
                    StringBuilder sb=new StringBuilder();
                    sb.append("您好,");
                    sb.append(teacher.getName());
                    sb.append("老师,\"");
                    sb.append(live.getCourse().getName());
                    sb.append("\"课程中给您安排的\"");
                    sb.append(live.getName());
                    sb.append("\"课程安排的");
                    sb.append(type.getText());
                    sb.append("已由");
                    sb.append(oldTeacher.getName());
                    sb.append("老师变更为");
                    sb.append(newTeacher.getName());
                    sb.append("老师");
                    SmsUtil.sendSms(teacher.getPhone(), sb.toString());
                    log.info("发送更换教师排课短信 : {} -> {}", teacher.getPhone(), sb.toString());
                }
            }
            courseLiveTeachers.add(oldData);
        }
    }

    @Override
    public void updateLastTeacher(Long teacherId, List<Long> liveIds) {
        //找出滚动课程ids
        List<BigInteger> rollIds = courseLiveTeacherRepository.findByTeacherIdAndsAndSourceLiveId(teacherId, liveIds);
//        courseLiveTeacherRepository.updateLastTeacher(teacherId,liveIds);
        if(!rollIds.isEmpty()){
             courseLiveTeacherRepository.updateLastTeacher(teacherId,liveIds,rollIds.stream().map(BigInteger::longValue).collect(Collectors.toList()));
        }else {
            courseLiveTeacherRepository.updateLastTeacher(teacherId, liveIds);
        }
    }

    @Override
    @Transactional
    public void deleteX(Long id) {
        courseLiveTeacherRepository.deleteByIdOrSourceId(id,id);
    }

    @Override
    @Transactional
    public void saveX(Long courseLiveId) {
        Long subjectId = courseRepository.getCourseSubjectId(courseLiveId);//课程指定科目
        CourseLive one = courseLiveRepository.findOne(courseLiveId);
        CourseLiveTeacher courseLiveTeacher = new CourseLiveTeacher();
        courseLiveTeacher.setCourseLiveId(one.getId());
        courseLiveTeacher.setConfirm(CourseConfirmStatus.DQR);//设置默认确认状态为待确认
        courseLiveTeacher.setSubjectId(subjectId);
        courseLiveTeacher.setTeacherCourseLevel(TeacherCourseLevel.GOOD);//默认专长级别
        courseLiveTeacher.setTeacherType(TeacherType.JS);  //类型添加讲师
        courseLiveTeacher=courseLiveTeacherRepository.save(courseLiveTeacher);

        List<Long> ids = courseLiveRepository.findBySourceIdIn(Lists.newArrayList(courseLiveId));
        for (Long id : ids) {
            CourseLiveTeacher bean = new CourseLiveTeacher();
            bean.setCourseLiveId(id);
            bean.setConfirm(CourseConfirmStatus.DQR);//设置默认确认状态为待确认
            bean.setSubjectId(subjectId);
            bean.setTeacherCourseLevel(TeacherCourseLevel.GOOD);//默认专长级别
            bean.setTeacherType(TeacherType.JS);  //类型添加讲师
            bean.setSourceId(courseLiveTeacher.getId());
            courseLiveTeacherRepository.save(bean);
        }
    }

    @Override
    public CourseLiveTeacher createCourseLiveAssistant(Long courseLiveId) {
        Long subjectId = courseRepository.getCourseSubjectId(courseLiveId);//课程指定科目
        CourseLiveTeacher courseLiveTeacher = new CourseLiveTeacher();
        courseLiveTeacher.setCourseLiveId(courseLiveId);
        courseLiveTeacher.setConfirm(CourseConfirmStatus.DQR);//设置默认确认状态为待确认
        courseLiveTeacher.setSubjectId(subjectId);
        courseLiveTeacher.setTeacherCourseLevel(TeacherCourseLevel.ZZZJ);//默认专长级别
        courseLiveTeacher.setTeacherType(TeacherType.ZJ);  //类型添加助教
        courseLiveTeacher=courseLiveTeacherRepository.save(courseLiveTeacher);

        List<Long> ids = courseLiveRepository.findBySourceIdIn(Lists.newArrayList(courseLiveId));
        for (Long id : ids) {
            CourseLiveTeacher bean = new CourseLiveTeacher();
            bean.setCourseLiveId(id);
            bean.setConfirm(CourseConfirmStatus.DQR);//设置默认确认状态为待确认
            bean.setSubjectId(subjectId);
            bean.setTeacherCourseLevel(TeacherCourseLevel.ZZZJ);//默认专长级别
            bean.setTeacherType(TeacherType.ZJ);  //类型添加助教
            bean.setSourceId(courseLiveTeacher.getId());
            courseLiveTeacherRepository.save(bean);
        }
        return courseLiveTeacher;
    }
}
