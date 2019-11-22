package com.huatu.tiku.schedule.biz.service.imple;

import java.util.List;

import com.huatu.tiku.schedule.base.exception.BadRequestException;
import com.huatu.tiku.schedule.biz.enums.CourseConfirmStatus;
import com.huatu.tiku.schedule.biz.enums.CoursePhase;
import com.huatu.tiku.schedule.biz.enums.CourseStatus;
import com.huatu.tiku.schedule.biz.enums.TeacherCourseLevel;
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
import com.huatu.tiku.schedule.biz.util.DateformatUtil;

@Service
public class CourseLiveTeacherServiceImpl extends BaseServiceImpl<CourseLiveTeacher, Long>
		implements CourseLiveTeacherService {

	private final CourseLiveTeacherRepository courseLiveTeacherRepository;

	private final CourseLiveRepository courseLiveRepository;

	@Autowired
	public CourseLiveTeacherServiceImpl(CourseLiveTeacherRepository courseLiveTeacherRepository,
			CourseLiveRepository courseLiveRepository) {
		this.courseLiveTeacherRepository = courseLiveTeacherRepository;
		this.courseLiveRepository = courseLiveRepository;
	}

	@Override
	public int bindTeacher(Long courseLiveTeacherId, Long teacherId) {
		courseLiveTeacherRepository.cancelTaskTeacher(courseLiveTeacherId);//取消教师确认状态
		return courseLiveTeacherRepository.bindTeacher(courseLiveTeacherId, teacherId);
	}

	@Override
	@Transactional
	public List<CourseLiveTeacher> saveCourseLiveTeacherBatch(SaveCourseLiveTeacherBatchDto courseLiveTeacherBatchDto,boolean isCover) {
		List<CourseLiveTeacher> courseLiveTeachers = Lists.newArrayList();
		courseLiveTeacherBatchDto.getDates().forEach(date -> {
			Integer timeBegin = Integer.parseInt(courseLiveTeacherBatchDto.getTimes().get(0).replace(":", ""));
			Integer timeEnd = Integer.parseInt(courseLiveTeacherBatchDto.getTimes().get(1).replace(":", ""));

			// 获取课程直播
			CourseLive courseLive = courseLiveRepository.findOneByCourseIdAndDateAndTimeBeginAndTimeEnd(
					courseLiveTeacherBatchDto.getCourseId(), date, timeBegin, timeEnd);

			// 如果不存在先创建课程直播
			if (courseLive == null) {
				// 创建直播
				courseLive = new CourseLive();
				courseLive.setCourseId(courseLiveTeacherBatchDto.getCourseId());
				courseLive.setTimeBegin(timeBegin);
				courseLive.setTimeEnd(timeEnd);
				courseLive.setDate(date);
				courseLive.setDateInt(Integer.parseInt(DateformatUtil.format1(date)));

				courseLiveRepository.save(courseLive);
			}
			List<CourseLiveTeacher> courseLiveTeachersList = courseLive.getCourseLiveTeachers();
			for (CourseLiveTeacher next : courseLiveTeachersList) {
				if(isCover) {
					courseLiveTeacherRepository.deleteById(next.getId());
				}else{
					if (next.getTeacherCourseLevel() == null && next.getCoursePhase() == null && next.getSubjectId() == null) {
						courseLiveTeacherRepository.deleteById(next.getId());
					}
				}
			}

			// 创建直播教师
			CourseLiveTeacher courseLiveTeacher = new CourseLiveTeacher();
			courseLiveTeacher.setCourseLiveId(courseLive.getId());
			courseLiveTeacher.setCoursePhase(courseLiveTeacherBatchDto.getCoursePhase());
			courseLiveTeacher.setTeacherCourseLevel(courseLiveTeacherBatchDto.getTeacherCourseLevel());
			courseLiveTeacher.setSubjectId(courseLiveTeacherBatchDto.getSubjectId());
			courseLiveTeacher.setModuleId(courseLiveTeacherBatchDto.getModuleIdl());
			courseLiveTeacher.setConfirm(CourseConfirmStatus.DQR);//初始状态
			courseLiveTeacherRepository.save(courseLiveTeacher);

			// 返回课程直播教师
			courseLiveTeachers.add(courseLiveTeacher);
		});

		return courseLiveTeachers;
	}

	@Override
	public int updateTaskTeacher(Long teacherId, List<Long> ids, CourseConfirmStatus courseConfirmStatus) {
		return courseLiveTeacherRepository.updateTaskTeacher(teacherId,ids, courseConfirmStatus);
	}

	@Override
	public int savaCoursePhase(Long courseLiveTeacherId, CoursePhase coursePhase) {
		return courseLiveTeacherRepository.savaCoursePhase(courseLiveTeacherId,coursePhase);
	}

	@Override
	public int savaModule(Long courseLiveTeacherId, Long moduleId) {
		return courseLiveTeacherRepository.savaModule(courseLiveTeacherId,moduleId);
	}

	@Override
	public int savaSubject(Long courseLiveTeacherId, Long subjectId) {
		return courseLiveTeacherRepository.savaSubject(courseLiveTeacherId,subjectId);
	}

	@Override
	public int savaLiveTeacherLevel(Long courseLiveTeacherId, TeacherCourseLevel teacherCourseLevel) {
		return courseLiveTeacherRepository.savaLiveTeacherLevel(courseLiveTeacherId,teacherCourseLevel);
	}

	@Override
	public void findCourseStatusByCourseLiveTeacherId(Long courseLiveTeacherId) {
        CourseStatus status = courseLiveTeacherRepository.findCourseStatusByCourseLiveTeacherId(courseLiveTeacherId);
        if(status!=null&&status.equals(CourseStatus.ZBAP)){//如果直播安排状态 表示已经被运营撤销
            throw new BadRequestException("该课程已撤销,请勿操作");
        }
	}

	@Override
	public CourseLiveTeacher findByCourseLiveIdAndTeacherId(Long courseLiveId, Long teacherId) {
		return courseLiveTeacherRepository.findByCourseLiveIdAndTeacherId(courseLiveId, teacherId);
	}
}
