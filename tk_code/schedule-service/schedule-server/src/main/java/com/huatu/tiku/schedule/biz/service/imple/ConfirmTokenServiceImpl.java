package com.huatu.tiku.schedule.biz.service.imple;

import java.util.HashSet;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.huatu.tiku.schedule.biz.repository.CourseLiveTeacherRepository;
import org.springframework.stereotype.Service;

import com.huatu.tiku.schedule.base.service.impl.BaseServiceImpl;
import com.huatu.tiku.schedule.biz.domain.ConfirmToken;
import com.huatu.tiku.schedule.biz.domain.Course;
import com.huatu.tiku.schedule.biz.domain.CourseLiveTeacher;
import com.huatu.tiku.schedule.biz.enums.CourseConfirmStatus;
import com.huatu.tiku.schedule.biz.repository.ConfirmTokenRepository;
import com.huatu.tiku.schedule.biz.service.ConfirmTokenService;
import com.huatu.tiku.schedule.biz.service.CourseLiveTeacherService;
import com.huatu.tiku.schedule.biz.service.CourseService;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfirmTokenServiceImpl extends BaseServiceImpl<ConfirmToken, Long> implements ConfirmTokenService {

    private final ConfirmTokenRepository confirmTokenRepository;

    private final CourseService courseService;

    private final CourseLiveTeacherService courseLiveTeacherService;

    private final CourseLiveTeacherRepository courseLiveTeacherRepository;


    public ConfirmTokenServiceImpl(ConfirmTokenRepository confirmTokenRepository, CourseService courseService,
                                   CourseLiveTeacherRepository courseLiveTeacherRepository, CourseLiveTeacherService courseLiveTeacherService
    ) {
        this.confirmTokenRepository = confirmTokenRepository;
        this.courseService = courseService;
        this.courseLiveTeacherRepository = courseLiveTeacherRepository;
        this.courseLiveTeacherService = courseLiveTeacherService;
    }

    @Override
    @Transactional
	public void confirm(String token, List<Long> courseLiveTeacherIds, CourseConfirmStatus courseConfirmStatus) {
        List<Long> liveIds= Lists.newArrayList();
        List<CourseLiveTeacher> all = courseLiveTeacherRepository.findAll(courseLiveTeacherIds);
        for (CourseLiveTeacher courseLiveTeacher : all) {
            Integer result = confirmTokenRepository.checkToken(token, courseLiveTeacher.getId());
            if (result > 0) {
                courseLiveTeacherRepository.updateTaskTeacher(courseLiveTeacher.getId(), courseConfirmStatus);
                liveIds.add(courseLiveTeacher.getCourseLiveId());
                courseLiveTeacher.setConfirm(courseConfirmStatus);
            }
        }

        if(!CourseConfirmStatus.QR.equals(courseConfirmStatus)){
            return ;//不是确认操作结束
        }

        ConfirmToken confirmToken = confirmTokenRepository.findByToken(token);
        Course course = courseService.findOne(confirmToken.getSourseId());
        //如果是重新安排给相关教师发送短信
        HashSet<Long> set = Sets.newHashSet(liveIds);//去重
//        HashSet set = new HashSet(liveIds);//去重
        liveIds.clear();
        liveIds.addAll(set);
        liveIds.forEach(liveId->
                courseLiveTeacherService.sendSmsToAbout(liveId,confirmToken.getTeacherId())
        );
        courseLiveTeacherService.updateLastTeacher(confirmToken.getTeacherId(), liveIds);//修改原教师
		// 将课程状态更改
        courseService.updateStatus(course);
	}


    @Override
    public ConfirmToken findByToken(String token) {
        return this.confirmTokenRepository.findByToken(token);
    }

}
