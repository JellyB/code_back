package com.huatu.tiku.schedule.biz.service.imple;

import java.util.*;

import javax.persistence.criteria.*;

import com.huatu.tiku.schedule.biz.domain.*;
import com.huatu.tiku.schedule.biz.enums.*;
import com.huatu.tiku.schedule.biz.repository.*;
import com.huatu.tiku.schedule.biz.util.SmsUtil;
import com.huatu.tiku.schedule.biz.util.TimeRangeUtil;
import com.huatu.tiku.schedule.biz.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.jpa.criteria.OrderImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.schedule.base.exception.BadRequestException;
import com.huatu.tiku.schedule.base.service.impl.BaseServiceImpl;
import com.huatu.tiku.schedule.biz.bean.TeacherScoreBean;
import com.huatu.tiku.schedule.biz.service.CourseLiveService;
import com.huatu.tiku.schedule.biz.service.LiveRoomService;
import com.huatu.tiku.schedule.biz.service.TeacherService;
import com.huatu.tiku.schedule.biz.util.DateformatUtil;
import com.huatu.tiku.schedule.biz.util.TimeformatUtil;

@Service
@Slf4j
public class CourseLiveServiceImpl extends BaseServiceImpl<CourseLive, Long> implements CourseLiveService {

	private final CourseLiveRepository courseLiveRepository;

	private final CourseLiveTeacherRepository courseLiveTeacherRepository;

	private final CourseRepository courseRepository;

    private final TeacherService teacherService;

    private final LiveRoomService liveRoomService;

    private final ConfirmTokenRepository confirmTokenRepository;

    @Value("${api.host}")
    private String apiHost;

	@Autowired
	public CourseLiveServiceImpl(CourseLiveRepository courseLiveRepository,
                                 CourseLiveTeacherRepository courseLiveTeacherRepository, CourseRepository courseRepository,
                                 TeacherService teacherService, LiveRoomService liveRoomService, ConfirmTokenRepository confirmTokenRepository) {
		this.courseLiveRepository = courseLiveRepository;
		this.courseLiveTeacherRepository = courseLiveTeacherRepository;
		this.courseRepository = courseRepository;
		this.teacherService = teacherService;
		this.liveRoomService = liveRoomService;
        this.confirmTokenRepository = confirmTokenRepository;
    }

	@Override
	@Transactional
	public List<CourseLive> createCourseLive(Long courseId,Long subjectId, List<Date> dates, List<List<String>> times,Boolean token) {
		List<CourseLive> courseLives = Lists.newArrayList();
		if(null!=token&&token){
            Date begin = dates.get(0);//开始日期
            Date end = dates.get(1);//结束日期
            Calendar dateBegin = Calendar.getInstance();
            dateBegin.setTime(begin);
            dates.clear();
            while (dateBegin.getTime().getTime() <= end.getTime()) {
                dateBegin.roll(Calendar.DAY_OF_YEAR, true);
                dates.add(dateBegin.getTime());
            }
        }
        Course course = courseRepository.findOne(courseId);
        dates.forEach(date -> {
			times.forEach(time -> {
				Integer timeBegin = Integer.parseInt(time.get(0).replace(":", ""));
				Integer timeEnd = Integer.parseInt(time.get(1).replace(":", ""));

				// 判断直播是否已经存在
				Boolean existsFlag = courseLiveRepository.existsByCourseIdAndDateAndTimeBeginAndTimeEnd(
						courseId, date, timeBegin, timeEnd);

				// 如果不存在创建
				if (!existsFlag) {
					// 创建直播
					CourseLive courseLive = new CourseLive();
					courseLive.setCourseId(courseId);
//					if(ExamType.MS.equals(course.getExamType())&&CourseCategory.XXK.equals(course.getCourseCategory())){//面试类型 且为线下课
					if(ExamType.MS.equals(course.getExamType())){//面试类型
                        courseLive.setCourseLiveCategory(CourseLiveCategory.SK);//默认授课类型
                    }
					courseLive.setLtConfirm(CourseConfirmStatus.DQR);//学习师确认状态
					courseLive.setAssConfirm(CourseConfirmStatus.DQR);
					courseLive.setCtrlConfirm(CourseConfirmStatus.DQR);
					courseLive.setComConfirm(CourseConfirmStatus.DQR);
					courseLive.setTimeBegin(timeBegin);
					courseLive.setTimeEnd(timeEnd);
					courseLive.setDate(date);
					courseLive.setDateInt(Integer.parseInt(DateformatUtil.format1(date)));

					courseLiveRepository.save(courseLive);

					// 创建直播教师
					CourseLiveTeacher courseLiveTeacher = new CourseLiveTeacher();
					courseLiveTeacher.setCourseLiveId(courseLive.getId());
					courseLiveTeacher.setConfirm(CourseConfirmStatus.DQR);
					courseLiveTeacher.setSubjectId(subjectId);
					courseLiveTeacher.setCoursePhase(CoursePhase.ONE);//默认一阶段
					courseLiveTeacher.setTeacherCourseLevel(TeacherCourseLevel.GOOD);//默认专长级别
					courseLiveTeacherRepository.save(courseLiveTeacher);

					// 返回课程直播教师
					courseLive.setCourseLiveTeachers(Arrays.asList(courseLiveTeacher));

					courseLives.add(courseLive);
				}
			});
		});

		return courseLives;
	}

	@Override
	public Boolean existsByCourseIdAndDateAndTimeBeginAndTimeEnd(Long courseId, Date date, Integer timeBegin,
			Integer timeEnd) {
		return courseLiveRepository.existsByCourseIdAndDateAndTimeBeginAndTimeEnd(courseId, date, timeBegin, timeEnd);
	}

	@Override
	public CourseLive findOneByCourseIdAndDateAndTimeBeginAndTimeEnd(Long courseId, Date date, Integer timeBegin,
			Integer timeEnd) {
		return courseLiveRepository.findOneByCourseIdAndDateAndTimeBeginAndTimeEnd(courseId, date, timeBegin, timeEnd);
	}

    /**
     * 根据教师类型 相应寻找
     * @param teacher 教师
     * @return
     */
	@Override
	public List<TaskLiveVo> findTaskTeacher(Teacher teacher) {
        TeacherType teacherType = teacher.getTeacherType();
        List<CourseLive> courseLivesDQR=null;
        List<CourseLive> courseLivesDGT=null;
        Long teacherId = teacher.getId();
        switch (teacherType){
            case JS:
				courseLivesDQR=courseLiveRepository.findTaskTeacherByTeacherId(teacherId,CourseConfirmStatus.DQR.ordinal(),CourseStatus.JSQR.ordinal());
				courseLivesDGT=courseLiveRepository.findTaskTeacherByTeacherId(teacherId,CourseConfirmStatus.DGT.ordinal(),CourseStatus.JSQR.ordinal());
                break;
            case ZJ:
				courseLivesDQR=courseLiveRepository.getAllByAssistantAndAssConfirm(teacherId,CourseConfirmStatus.DQR.ordinal(),CourseStatus.ZJQR.ordinal());
				courseLivesDGT=courseLiveRepository.getAllByAssistantAndAssConfirm(teacherId,CourseConfirmStatus.DGT.ordinal(),CourseStatus.ZJQR.ordinal());
                break;
            case XXS:
				courseLivesDQR=courseLiveRepository.getAllByLearningTeacherAndLtConfirm(teacherId,CourseConfirmStatus.DQR.ordinal(),CourseStatus.ZJQR.ordinal());
				courseLivesDGT=courseLiveRepository.getAllByLearningTeacherAndLtConfirm(teacherId,CourseConfirmStatus.DGT.ordinal(),CourseStatus.ZJQR.ordinal());
                break;
            case CK:
				courseLivesDQR=courseLiveRepository.getAllByControllerAndCtrlConfirm(teacherId,CourseConfirmStatus.DQR.ordinal(),CourseStatus.ZJQR.ordinal());
				courseLivesDGT=courseLiveRepository.getAllByControllerAndCtrlConfirm(teacherId,CourseConfirmStatus.DGT.ordinal(),CourseStatus.ZJQR.ordinal());
                break;
            case ZCR:
				courseLivesDQR=courseLiveRepository.getAllByCompereAndComConfirm(teacherId,CourseConfirmStatus.DQR.ordinal(),CourseStatus.ZJQR.ordinal());
				courseLivesDGT=courseLiveRepository.getAllByCompereAndComConfirm(teacherId,CourseConfirmStatus.DGT.ordinal(),CourseStatus.ZJQR.ordinal());
                break;
        }
		List<TaskLiveVo> taskLiveVos=new ArrayList<>();
		courseLivesDQR.forEach(taskLive->{//封装结果集
			TaskLiveVo taskLiveVo=new TaskLiveVo();
			taskLiveVo.setLiveId(taskLive.getId());
			taskLiveVo.setLiveName(taskLive.getName());
			taskLiveVo.setCourseId(taskLive.getCourseId());
			taskLiveVo.setCourseName(taskLive.getCourse().getName());
			Integer begin = taskLive.getTimeBegin();
			Integer end = taskLive.getTimeEnd();
			if(null!=begin&&null!=end){
				String timeBegin = TimeRangeUtil.intToDateString(begin);
				String timeEnd = TimeRangeUtil.intToDateString(end);
				taskLiveVo.setTimeRange(taskLive.getDate().toString()+" "+timeBegin+"-"+timeEnd);
			}
			taskLiveVo.setConfirmKey(CourseConfirmStatus.DQR.getValue());
			taskLiveVo.setConfirmStatus(CourseConfirmStatus.DQR.getText());
			taskLiveVos.add(taskLiveVo);
		});
		courseLivesDGT.forEach(taskLive->{//封装结果集
			TaskLiveVo taskLiveVo=new TaskLiveVo();
			taskLiveVo.setLiveId(taskLive.getId());
			taskLiveVo.setLiveName(taskLive.getName());
			taskLiveVo.setCourseId(taskLive.getCourseId());
			taskLiveVo.setCourseName(taskLive.getCourse().getName());
			Integer begin = taskLive.getTimeBegin();
			Integer end = taskLive.getTimeEnd();
			if(null!=begin&&null!=end){
				String timeBegin = TimeRangeUtil.intToDateString(begin);
				String timeEnd = TimeRangeUtil.intToDateString(end);
				taskLiveVo.setTimeRange(taskLive.getDate().toString()+" "+timeBegin+"-"+timeEnd);
			}
			taskLiveVo.setConfirmKey(CourseConfirmStatus.DGT.getValue());
			taskLiveVo.setConfirmStatus(CourseConfirmStatus.DGT.getText());
			taskLiveVos.add(taskLiveVo);
		});
        return taskLiveVos;
    }


	@Override
	public Page<CourseLiveScheduleVo> schedule(List<ExamType> examTypes, Long subjectId, Date dateBegin, Date dateEnd,
                                               Long teacherId ,Pageable page) {
		List<Sort.Order> list=new ArrayList();
		list.add(new Sort.Order(Sort.Direction.ASC, "dateInt"));//日期
		list.add(new Sort.Order(Sort.Direction.ASC, "timeBegin"));//开始时间
		list.add(new Sort.Order(Sort.Direction.ASC, "timeEnd"));//结束时间
		Pageable pageable=new PageRequest(page.getPageNumber(),page.getPageSize(),new Sort(list));
		Page<CourseLive> pages = courseLiveRepository.findAll(new Specification<CourseLive>() {
			@Override
			public Predicate toPredicate(Root<CourseLive> root, CriteriaQuery<?> criteriaQuery,
					CriteriaBuilder criteriaBuilder) {
				criteriaQuery.distinct(true);

//				criteriaQuery.orderBy(new OrderImpl(root.get("timeBegin")));

				List<Predicate> predicates = new ArrayList<>();

				Join<CourseLive, Course> course = root.join("course");
				predicates.add(criteriaBuilder.equal(course.get("status"), CourseStatus.WC));
				if (examTypes != null && !examTypes.isEmpty()) {
					predicates.add(course.get("examType").in(examTypes));
				}

				if (subjectId != null) {
					predicates.add(criteriaBuilder.equal(course.get("subjectId"), subjectId));
				}

				if (dateBegin != null) {
					predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("date"), dateBegin));
				}

				if (dateEnd != null) {
					predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("date"), dateEnd));
				}

				Join<CourseLive, CourseLiveTeacher> courseLiveTeachers = root.join("courseLiveTeachers");

//				if (!Strings.isNullOrEmpty(teacherName)) {
//					if (courseLiveTeachers == null) {
//						courseLiveTeachers = root.join("courseLiveTeachers");
//					}
//
//					predicates.add(
//							criteriaBuilder.like(criteriaBuilder.upper(courseLiveTeachers.join("teacher").get("name")),
//									"%" + teacherName.toUpperCase() + "%"));
//				}
                if(teacherId!=null){
                    predicates.add(criteriaBuilder.equal(courseLiveTeachers.get("teacherId"),teacherId));
                }


				return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		},pageable);
		List<CourseLive> courseLives = pages.getContent();
		// 直播
		List<CourseLiveScheduleVo> courseLiveScheduleVos = Lists.newArrayList();

		// 直播字典
		Map<Long, CourseLiveScheduleVo> courseLiveDic = Maps.newHashMap();

		courseLives.forEach(courseLive -> {
			CourseLiveScheduleVo courseLiveScheduleVo = new CourseLiveScheduleVo();

			courseLiveScheduleVo.setId(courseLive.getId());
			courseLiveScheduleVo.setDate(
					DateformatUtil.format2(courseLive.getDate()) + " " + DateformatUtil.format4(courseLive.getDate()));
			courseLiveScheduleVo.setTimeBegin(TimeformatUtil.format(courseLive.getTimeBegin()));
			courseLiveScheduleVo.setTimeEnd(TimeformatUtil.format(courseLive.getTimeEnd()));
			courseLiveScheduleVo.setCourseName(courseLive.getCourse().getName());
			courseLiveScheduleVo.setCategoryName(courseLive.getCourse().getCourseCategory().getText());
			if (courseLive.getCourse().getAssistantFlag()) {
				if (courseLive.getAssistant() != null) {
					courseLiveScheduleVo.setAssistantName(courseLive.getAssistant().getName());
				}
			}

			if (courseLive.getCourse().getControllerFlag()) {
				if (courseLive.getController() != null) {
					courseLiveScheduleVo.setControllerName(courseLive.getController().getName());
				}
			}

			if (courseLive.getCourse().getCompereFlag()) {
				if (courseLive.getCompere() != null) {
					courseLiveScheduleVo.setCompereName(courseLive.getCompere().getName());
				}
			}

			if (courseLive.getCourse().getLearningTeacherFlag()) {
				if (courseLive.getLearningTeacher() != null) {
					courseLiveScheduleVo.setLearningTeacherName(courseLive.getLearningTeacher().getName());
				}
			}

			List<CourseLiveScheduleVo.TeacherInfo> teacherInfos = Lists.newArrayList();

			courseLive.getCourseLiveTeachers().forEach(courseLiveTeacher -> {
				CourseLiveScheduleVo.TeacherInfo teacherInfo = new CourseLiveScheduleVo.TeacherInfo();

				if (courseLiveTeacher.getCoursePhase() != null) {
					teacherInfo.setPhase(courseLiveTeacher.getCoursePhase().getText());
				}

				if (courseLiveTeacher.getSubject() != null) {
					teacherInfo.setSubject(courseLiveTeacher.getSubject().getName());
				}

				if (courseLiveTeacher.getModule() != null) {
					teacherInfo.setModel(courseLiveTeacher.getModule().getName());
				}

				if (courseLiveTeacher.getTeacher() != null) {
					teacherInfo.setName(courseLiveTeacher.getTeacher().getName());

					teacherInfos.add(teacherInfo);
				}
			});

			courseLiveScheduleVo.setTeacherInfos(teacherInfos);

			courseLiveScheduleVo.setCourseLiveName(courseLive.getName());

			courseLiveScheduleVo.setExamType(courseLive.getCourse().getExamType().getText());

			Subject subject = courseLive.getCourse().getSubject();
			if (subject != null) {
				courseLiveScheduleVo.setSubject(subject.getName());
			}

			courseLiveScheduleVo.setSourceId(courseLive.getSourceId());

			courseLiveScheduleVos.add(courseLiveScheduleVo);

			courseLiveDic.put(courseLive.getId(), courseLiveScheduleVo);
		});

		// 处理滚动排课
		Iterator<CourseLiveScheduleVo> courseLiveScheduleVoIterator = courseLiveScheduleVos.iterator();
		while (courseLiveScheduleVoIterator.hasNext()) {
			CourseLiveScheduleVo temp = courseLiveScheduleVoIterator.next();
			// 如果是滚动排课，则只在源课程中追加课程名称
			if (temp.getSourceId() != null) {
				CourseLiveScheduleVo sourceCourseLive = courseLiveDic.get(temp.getSourceId());
				if (sourceCourseLive != null) {
					sourceCourseLive.setCourseName(sourceCourseLive.getCourseName() + "，" + temp.getCourseName());

					courseLiveScheduleVoIterator.remove();
				}
			}
		}
		return new PageImpl(courseLiveScheduleVos,page,courseLiveScheduleVos == null ? 0 : pages.getTotalElements());
	}

    @Override
    public int updateTaskTeacher(Teacher teacher, List<Long> liveIds, CourseConfirmStatus courseConfirmStatus) {
	    int count=0;
	    switch (teacher.getTeacherType()){
            case ZJ:
                count=courseLiveRepository.updateTaskAssistant(teacher.getId(),liveIds, courseConfirmStatus);
                break;
            case XXS:
                count=courseLiveRepository.updateTaskLearningTeacher(teacher.getId(),liveIds, courseConfirmStatus);
                break;
            case CK:
                count=courseLiveRepository.updateTaskController(teacher.getId(),liveIds, courseConfirmStatus);
                break;
            case ZCR:
                count=courseLiveRepository.updateTaskCompere(teacher.getId(),liveIds, courseConfirmStatus);
                break;
        }
        return count;
    }

    @Override
    public int updateLiveRoom(Long liveId, Long roomId) {
        return courseLiveRepository.updateLiveRoom(roomId,liveId);
    }

	@Override
	public List<CourseLiveRollingVo> findForRolling(Long currentCourseId, List<Date> dates, Long courseId,
			ExamType examType, Long subjectId) {
		List<CourseLive> courseLives = courseLiveRepository.findAll(new Specification<CourseLive>() {
			@Override
			public Predicate toPredicate(Root<CourseLive> root, CriteriaQuery<?> criteriaQuery,
					CriteriaBuilder criteriaBuilder) {
				List<Predicate> predicates = new ArrayList<>();

				Join<CourseLive, Course> course = root.join("course");

				predicates.add(criteriaBuilder.notEqual(course.get("id"), currentCourseId));

				predicates.add(criteriaBuilder.equal(course.get("status"), CourseStatus.WC));

                predicates.add(criteriaBuilder.equal(course.get("courseCategory"), CourseCategory.LIVE));//直播课程

				if (dates != null && !dates.isEmpty()) {
					predicates.add(root.get("date").in(dates));
				}

				if (examType != null) {
					predicates.add(criteriaBuilder.equal(course.get("examType"), examType));
				}

				if (subjectId != null) {
					predicates.add(criteriaBuilder.equal(course.get("subjectId"), subjectId));
				}

				if (courseId != null) {
					predicates.add(criteriaBuilder.equal(root.get("courseId"), courseId));
				}

				return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		});

		List<CourseLiveRollingVo> courseLiveRollingVos = Lists.newArrayList();

		courseLives.forEach(courseLive -> {
			CourseLiveRollingVo courseLiveRollingVo = new CourseLiveRollingVo();
			courseLiveRollingVo.setId(courseLive.getId());
			courseLiveRollingVo.setCourseName(courseLive.getCourse().getName());
			courseLiveRollingVo.setCourseLiveName(courseLive.getName());

			Subject subject = courseLive.getCourse().getSubject();

			if (subject != null) {
				courseLiveRollingVo.setSubject(subject.getName());
			}

			courseLiveRollingVo.setDate(DateformatUtil.format2(courseLive.getDate()));
			courseLiveRollingVo.setTimeBegin(TimeformatUtil.format(courseLive.getTimeBegin()));
			courseLiveRollingVo.setTimeEnd(TimeformatUtil.format(courseLive.getTimeEnd()));

			courseLiveRollingVos.add(courseLiveRollingVo);
		});

		return courseLiveRollingVos;
	}

	@Override
	@Transactional
	public void rollingSchedule(Long courseId, List<Long> courseLiveIds) {
		// 获取当前课程
		Course course = courseRepository.findOne(courseId);
		if (course == null) {
			throw new BadRequestException("课程不存在");
		}

		// 获取课程直播
		List<CourseLive> courseLives = courseLiveRepository.findAll(courseLiveIds);
		if (courseLiveIds.isEmpty()) {
			throw new BadRequestException("课程直播不存在");
		}

		// 判断课程是否已包含直播
		courseLives.forEach(courseLive -> {
			// 滚动排课源
			Long sourceId = courseLive.getId();

			// 获取课程原有直播
			CourseLive courseLiveTemp = courseLiveRepository.findOneByCourseIdAndDateAndTimeBeginAndTimeEnd(courseId,
					courseLive.getDate(), courseLive.getTimeBegin(), courseLive.getTimeEnd());
			if (courseLiveTemp == null) {
				// 不存在，直接创建
				courseLiveTemp = new CourseLive();
			} else {
				// 已经存在，判断是否已经关联删除课程直播教师
				courseLiveTeacherRepository.delete(courseLiveTemp.getCourseLiveTeachers());
				courseLiveTemp.setCourseLiveTeachers(null);
			}

			BeanUtils.copyProperties(courseLive, courseLiveTemp, "id", "course", "liveRoom", "assistant", "controller",
					"compere", "courseLiveTeachers");

			courseLiveTemp.setCourseId(course.getId());
			courseLiveTemp.setSourceId(sourceId);

			courseLiveRepository.save(courseLiveTemp);

			// 复制courseLiveTeachers
			List<CourseLiveTeacher> courseLiveTeachers = courseLive.getCourseLiveTeachers();

			Long courseLiveId = courseLiveTemp.getId();

			List<CourseLiveTeacher> courseliveteachersTemp = Lists.newArrayList();

			courseLiveTeachers.forEach(courseLiveTeacher -> {
				CourseLiveTeacher courseLiveTeacherTemp = new CourseLiveTeacher();

				BeanUtils.copyProperties(courseLiveTeacher, courseLiveTeacherTemp, "id", "courseLive", "module",
						"teacher");

				courseLiveTeacherTemp.setCourseLiveId(courseLiveId);

				courseliveteachersTemp.add(courseLiveTeacherTemp);
			});

			courseLiveTeacherRepository.save(courseliveteachersTemp);
		});
	}

	@Override
	public int updateAssistant(Long liveId, Long teacherId, TeacherType teacherType) {
		int result=0;
		switch (teacherType){
			case ZJ:
				result=courseLiveRepository.updateAssistant(liveId,teacherId,CourseConfirmStatus.DQR);
				break;
			case XXS:
				result=courseLiveRepository.updateLearningTeacher(liveId,teacherId,CourseConfirmStatus.DQR);
				break;
			case CK:
				result=courseLiveRepository.updateController(liveId,teacherId,CourseConfirmStatus.DQR);
				break;
			case ZCR:
				result=courseLiveRepository.updateCompere(liveId,teacherId,CourseConfirmStatus.DQR);
				break;
		}
		return result;
	}

	@Override
	public List<CourseLive> findByCourseIdAndDateIn(Long courseId, List<Date> dates) {
		return courseLiveRepository.findByCourseIdAndDateIn(courseId, dates);
	}

	@Override
	public int updateLiveName(Long liveId, String liveName) {
		return courseLiveRepository.updateLiveName(liveId, liveName);
	}

	@Override
	public void oneKeySchedule(Long id) {
		// 获取课程
		Course course = courseRepository.findOne(id);
		// 遍历课程直播
		course.getCourseLives().forEach(courseLive -> {
			// 排直播间
			if (courseLive.getLiveRoomId() == null) {
				List<LiveRoom> liveRooms = liveRoomService.getAvailable(courseLive.getDateInt(),
						courseLive.getTimeBegin(), courseLive.getTimeEnd());
				courseLive.setLiveRoomId(liveRooms.get(0).getId());

				courseLiveRepository.save(courseLive);
			}
			courseLive.getCourseLiveTeachers().forEach(courseLiveTeacher -> {
				// 指定教师
				if (courseLiveTeacher.getTeacherId() == null) {
					List<TeacherScoreBean> teachers = teacherService.autoGetAvailableTeachers(courseLive.getDate(),
							courseLive.getTimeBegin(), courseLive.getTimeEnd(), course.getExamType(),
							course.getSubjectId(), courseLiveTeacher.getTeacherCourseLevel(),
                            course.getId(),courseLiveTeacher.getModuleId());

					courseLiveTeacher.setTeacherId(teachers.get(0).getId());

					courseLiveTeacherRepository.save(courseLiveTeacher);
				}
			});
		});
	}

	@Override
	public void oneKeySchedule(Long courseId, List<Date> dates) {
		// 获取直播
		List<CourseLive> courseLives = courseLiveRepository.findByCourseIdAndDateIn(courseId, dates);

		// 课程
		Course course = courseLives.get(0).getCourse();

		// 遍历课程直播
		courseLives.forEach(courseLive -> {

			courseLive.getCourseLiveTeachers().forEach(courseLiveTeacher -> {
				// 指定教师
				if (courseLiveTeacher.getTeacherId() == null) {
					List<TeacherScoreBean> teachers = teacherService.autoGetAvailableTeachers(courseLive.getDate(),
							courseLive.getTimeBegin(), courseLive.getTimeEnd(), course.getExamType(),
							courseLiveTeacher.getSubjectId(), courseLiveTeacher.getTeacherCourseLevel(),
                            course.getId(),courseLiveTeacher.getModuleId());
					if (teachers.size() > 0) {
						courseLiveTeacher.setTeacherId(teachers.get(0).getId());
                        courseLiveTeacherRepository.save(courseLiveTeacher);
					}
				}
			});
		});
	}

	@Override
	public void findCourseStatusByCourseLiveId(Long liveId) {
		CourseStatus status = courseLiveRepository.findCourseStatusByCourseLiveId(liveId);
		if(status!=null&&status.equals(CourseStatus.ZBAP)){//如果直播安排状态 表示已经被运营撤销
			throw new BadRequestException("该课程已撤销,请勿操作");
		}
	}

	@Override
	public Page<CourseLiveScheduleAssistantVo> scheduleAssistant(String courseName, ExamType examType, Long subjectId,
			Long liveRoomId, Date dateBegin, Date dateEnd, TeacherType teacherType, String teacherName,Pageable page) {
        List<Sort.Order> list=new ArrayList();
        list.add(new Sort.Order(Sort.Direction.ASC, "dateInt"));//日期
        list.add(new Sort.Order(Sort.Direction.ASC, "timeBegin"));//开始时间
        list.add(new Sort.Order(Sort.Direction.ASC, "timeEnd"));//结束时间
        Pageable pageable=new PageRequest(page.getPageNumber(),page.getPageSize(),new Sort(list));
		Page<CourseLive> pages = courseLiveRepository.findAll(new Specification<CourseLive>() {
			@Override
			public Predicate toPredicate(Root<CourseLive> root, CriteriaQuery<?> criteriaQuery,
					CriteriaBuilder criteriaBuilder) {
				List<Predicate> predicates = new ArrayList<>();

				Join<CourseLive, Course> course = root.join("course");
				predicates.add(criteriaBuilder.equal(course.get("status"), CourseStatus.WC));
				// 课程名称
				if (!Strings.isNullOrEmpty(courseName)) {
					predicates.add(criteriaBuilder.like(course.get("name"), "%" + courseName + "%"));
				}

				// 考试类型
				if (examType != null) {
					predicates.add(criteriaBuilder.equal(course.get("examType"), examType));
				}

				// 科目ID
				if (subjectId != null) {
					predicates.add(criteriaBuilder.equal(course.get("subjectId"), subjectId));
				}

				// 直播间
				if (liveRoomId != null) {
					predicates.add(criteriaBuilder.equal(root.get("liveRoomId"), liveRoomId));
				}

				// 开始时间
				if (dateBegin != null) {
					predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("date"), dateBegin));
				}

				// 结束时间
				if (dateEnd != null) {
					predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("date"), dateEnd));
				}

				// 助教姓名
				if (Strings.isNullOrEmpty(teacherName)) {
				    if(teacherType!=null){
				        switch (teacherType){
                            case ZJ:
                                predicates.add(criteriaBuilder.isNotNull(root.get("assistantId")));
                                break;
                            case ZCR:
                                predicates.add(criteriaBuilder.isNotNull(root.get("compereId")));
                                break;
                            case XXS:
                                predicates.add(criteriaBuilder.isNotNull(root.get("learningTeacherId")));
                                break;
                            case CK:
                                predicates.add(criteriaBuilder.isNotNull(root.get("controllerId")));
                                break;
                        }
                    }else{
                        Predicate p1 = criteriaBuilder.isNotNull(root.get("assistantId"));
                        Predicate p2 = criteriaBuilder.isNotNull(root.get("controllerId"));
                        Predicate p3 = criteriaBuilder.isNotNull(root.get("compereId"));
                        Predicate p4 = criteriaBuilder.isNotNull(root.get("learningTeacherId"));
                        predicates.add(criteriaBuilder.or(p1,p2,p3,p4));
                    }
                } else {
                    if(teacherType!=null){
                        switch (teacherType){
                            case ZJ:
                                predicates.add(criteriaBuilder.like(criteriaBuilder.upper(root.join("assistant", JoinType.LEFT).get("name")),
                                        "%" + teacherName.toUpperCase() + "%"));
                                break;
                            case ZCR:
                                predicates.add(criteriaBuilder.like(criteriaBuilder.upper(root.join("compere", JoinType.LEFT).get("name")),
                                        "%" + teacherName.toUpperCase() + "%"));
                                break;
                            case XXS:
                                predicates.add(criteriaBuilder.like(criteriaBuilder.upper(root.join("learningTeacher", JoinType.LEFT).get("name")),
                                        "%" + teacherName.toUpperCase() + "%"));
                                break;
                            case CK:
                                predicates.add(criteriaBuilder.like(criteriaBuilder.upper(root.join("controller", JoinType.LEFT).get("name")),
                                        "%" + teacherName.toUpperCase() + "%"));
                                break;
                        }
                    }else {
                        // 匹配教师姓名
                        Predicate p1 = criteriaBuilder.like(criteriaBuilder.upper(root.join("assistant", JoinType.LEFT).get("name")),
                                "%" + teacherName.toUpperCase() + "%");
                        Predicate p2 = criteriaBuilder.like(criteriaBuilder.upper(root.join("controller", JoinType.LEFT).get("name")),
                                "%" + teacherName.toUpperCase() + "%");
                        Predicate p3 = criteriaBuilder.like(criteriaBuilder.upper(root.join("compere", JoinType.LEFT).get("name")),
                                "%" + teacherName.toUpperCase() + "%");
                        Predicate p4 = criteriaBuilder.like(criteriaBuilder.upper(root.join("learningTeacher", JoinType.LEFT).get("name")),
                                "%" + teacherName.toUpperCase() + "%");
                        predicates.add(criteriaBuilder.or(p1, p2, p3, p4));
                    }
				}

				return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		},pageable);
        List<CourseLive> courseLives=pages.getContent();
		// 直播
		List<CourseLiveScheduleAssistantVo> courseLiveScheduleVos = Lists.newArrayList();

		// 直播字典
		Map<Long, CourseLiveScheduleAssistantVo> courseLiveDic = Maps.newHashMap();

		courseLives.forEach(courseLive -> {
			CourseLiveScheduleAssistantVo courseLiveScheduleAssistantVo = new CourseLiveScheduleAssistantVo();

			courseLiveScheduleAssistantVo.setCourseLiveId(courseLive.getId());
			courseLiveScheduleAssistantVo.setDate(
					DateformatUtil.format2(courseLive.getDate()) + " " + DateformatUtil.format4(courseLive.getDate()));
			courseLiveScheduleAssistantVo.setTimeBegin(TimeformatUtil.format(courseLive.getTimeBegin()));
			courseLiveScheduleAssistantVo.setTimeEnd(TimeformatUtil.format(courseLive.getTimeEnd()));
			courseLiveScheduleAssistantVo.setCourseName(courseLive.getCourse().getName());
            courseLiveScheduleAssistantVo.setCategoryName(courseLive.getCourse().getCourseCategory().getText());
			if (courseLive.getCourse().getAssistantFlag()) {
				if (courseLive.getAssistant() != null) {
					courseLiveScheduleAssistantVo.setAssistantName(courseLive.getAssistant().getName());
				}
			}

			if (courseLive.getCourse().getControllerFlag()) {
				if (courseLive.getController() != null) {
					courseLiveScheduleAssistantVo.setControllerName(courseLive.getController().getName());
				}
			}

			if (courseLive.getCourse().getCompereFlag()) {
				if (courseLive.getCompere() != null) {
					courseLiveScheduleAssistantVo.setCompereName(courseLive.getCompere().getName());
				}
			}

			if (courseLive.getCourse().getLearningTeacherFlag()) {
				if (courseLive.getLearningTeacher() != null) {
					courseLiveScheduleAssistantVo.setLearningTeacherName(courseLive.getLearningTeacher().getName());
				}
			}

			courseLiveScheduleAssistantVo.setCourseLiveName(courseLive.getName());

			courseLiveScheduleAssistantVo.setExamType(courseLive.getCourse().getExamType().getText());

			Subject subject = courseLive.getCourse().getSubject();

			if (subject != null) {
				courseLiveScheduleAssistantVo.setSubject(subject.getName());
			}

			courseLiveScheduleAssistantVo.setSourceId(courseLive.getSourceId());

			StringBuffer sb=new StringBuffer();
            courseLive.getCourseLiveTeachers().forEach(liveTeacher->{
                Teacher teacher = liveTeacher.getTeacher();
                if(teacher!=null){
                    sb.append(teacher.getName());
                    sb.append(",");
                }
            });
            if(sb.length()>1){
                sb.deleteCharAt(sb.length()-1);
                String teacherNames = sb.toString();
                if(teacherNames!=null&&!teacherNames.isEmpty()&&teacherNames.length()>=1){
                    courseLiveScheduleAssistantVo.setTeacherNames(teacherNames);
                }
            }

			courseLiveScheduleVos.add(courseLiveScheduleAssistantVo);

			courseLiveDic.put(courseLive.getId(), courseLiveScheduleAssistantVo);
		});

		// 处理滚动排课
		Iterator<CourseLiveScheduleAssistantVo> courseLiveScheduleAssistantVoIterator = courseLiveScheduleVos.iterator();
		while (courseLiveScheduleAssistantVoIterator.hasNext()) {
			CourseLiveScheduleAssistantVo temp = courseLiveScheduleAssistantVoIterator.next();
			// 如果是滚动排课，则只在源课程中追加课程名称
			if (temp.getSourceId() != null) {
				CourseLiveScheduleAssistantVo sourceCourseLive = courseLiveDic.get(temp.getSourceId());
				if (sourceCourseLive != null) {
					sourceCourseLive.setCourseName(sourceCourseLive.getCourseName() + "，" + temp.getCourseName());

					courseLiveScheduleAssistantVoIterator.remove();
				}
			}
		}
        return new PageImpl(courseLiveScheduleVos,page,courseLiveScheduleVos == null ? 0 : pages.getTotalElements());
	}

	@Override
	public List<CourseLiveScheduleAssistantVo> myScheduleAssistant(String courseName, ExamType examType, Long subjectId,
			Long liveRoomId, Date dateBegin, Date dateEnd, TeacherType teacherType, Long teacherId) {
		List<CourseLive> courseLives = courseLiveRepository.findAll(new Specification<CourseLive>() {
			@Override
			public Predicate toPredicate(Root<CourseLive> root, CriteriaQuery<?> criteriaQuery,
					CriteriaBuilder criteriaBuilder) {
				List<Predicate> predicates = new ArrayList<>();

				Join<CourseLive, Course> course = root.join("course");
				predicates.add(criteriaBuilder.equal(course.get("status"), CourseStatus.WC));
				// 课程名称
				if (!Strings.isNullOrEmpty(courseName)) {
					predicates.add(criteriaBuilder.like(course.get("name"), "%" + courseName + "%"));
				}

				// 考试类型
				if (examType != null) {
					predicates.add(criteriaBuilder.equal(course.get("examType"), examType));
				}

				// 科目ID
				if (subjectId != null) {
					predicates.add(criteriaBuilder.equal(course.get("subjectId"), subjectId));
				}

				// 直播间
				if (liveRoomId != null) {
					predicates.add(criteriaBuilder.equal(root.get("liveRoomId"), liveRoomId));
				}

				// 开始时间
				if (dateBegin != null) {
					predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("date"), dateBegin));
				}

				// 结束时间
				if (dateEnd != null) {
					predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("date"), dateEnd));
				}

				// 匹配教师
				String field = null;

				if (TeacherType.ZJ.equals(teacherType)) {
					field = "assistantId";
				} else if (TeacherType.CK.equals(teacherType)) {
					field = "controllerId";
				} else if (TeacherType.ZCR.equals(teacherType)) {
					field = "compereId";
				} else if (TeacherType.XXS.equals(teacherType)) {
					field = "learningTeacherId";
				}

				predicates.add(criteriaBuilder.equal(root.get(field), teacherId));

				return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		});

		// 直播
		List<CourseLiveScheduleAssistantVo> courseLiveScheduleVos = Lists.newArrayList();

		// 直播字典
		Map<Long, CourseLiveScheduleAssistantVo> courseLiveDic = Maps.newHashMap();

		courseLives.forEach(courseLive -> {
			CourseLiveScheduleAssistantVo courseLiveScheduleAssistantVo = new CourseLiveScheduleAssistantVo();

			courseLiveScheduleAssistantVo.setCourseLiveId(courseLive.getId());
			courseLiveScheduleAssistantVo.setDate(
					DateformatUtil.format0(courseLive.getDate() ) );
			courseLiveScheduleAssistantVo.setTimeBegin(TimeformatUtil.format(courseLive.getTimeBegin()));
			courseLiveScheduleAssistantVo.setTimeEnd(TimeformatUtil.format(courseLive.getTimeEnd()));
			courseLiveScheduleAssistantVo.setCourseName(courseLive.getCourse().getName());
			courseLiveScheduleAssistantVo.setCategoryName(courseLive.getCourse().getCourseCategory().getText());
			courseLiveScheduleAssistantVo.setPlace(courseLive.getCourse().getPlace());

			if(null!=courseLive.getAssistant()) {
				courseLiveScheduleAssistantVo.setAssistantName(courseLive.getAssistant().getName());
			}
			if(null!=courseLive.getController()) {
				courseLiveScheduleAssistantVo.setControllerName(courseLive.getController().getName());
			}
			if(null!=courseLive.getCompere()) {
				courseLiveScheduleAssistantVo.setCompereName(courseLive.getCompere().getName());
			}
			if(null!=courseLive.getLearningTeacher()) {
				courseLiveScheduleAssistantVo.setLearningTeacherName(courseLive.getLearningTeacher().getName());
			}

			courseLiveScheduleAssistantVo.setCourseLiveName(courseLive.getName());

			courseLiveScheduleAssistantVo.setExamType(courseLive.getCourse().getExamType().getText());

			Subject subject = courseLive.getCourse().getSubject();

			if (subject != null) {
				courseLiveScheduleAssistantVo.setSubject(subject.getName());
			}

			courseLiveScheduleAssistantVo.setSourceId(courseLive.getSourceId());

			StringBuffer sb=new StringBuffer();
            courseLive.getCourseLiveTeachers().forEach(liveTeacher->{
                Teacher teacher = liveTeacher.getTeacher();
                if(teacher!=null){
                    sb.append(teacher.getName());
                    sb.append(",");
                }
            });
            if(sb.length()>1){
				sb.deleteCharAt(sb.length()-1);
			}
            String teacherNames = sb.toString();
            if(teacherNames!=null&&!teacherNames.isEmpty()&&teacherNames.length()>=1){
                courseLiveScheduleAssistantVo.setTeacherNames(teacherNames);
            }

            courseLiveScheduleVos.add(courseLiveScheduleAssistantVo);

			courseLiveDic.put(courseLive.getId(), courseLiveScheduleAssistantVo);
		});

		// 处理滚动排课
		Iterator<CourseLiveScheduleAssistantVo> courseLiveScheduleAssistantVoIterator = courseLiveScheduleVos.iterator();
		while (courseLiveScheduleAssistantVoIterator.hasNext()) {
			CourseLiveScheduleAssistantVo temp = courseLiveScheduleAssistantVoIterator.next();
			// 如果是滚动排课，则只在源课程中追加课程名称
			if (temp.getSourceId() != null) {
				CourseLiveScheduleAssistantVo sourceCourseLive = courseLiveDic.get(temp.getSourceId());
				if (sourceCourseLive != null) {
					sourceCourseLive.setCourseName(sourceCourseLive.getCourseName() + "，" + temp.getCourseName());

					courseLiveScheduleAssistantVoIterator.remove();
				}
			}
		}

		return courseLiveScheduleVos;
	}

	@Override
	public List<CourseLiveScheduleVo> mySchedule(ExamType examType, Long subjectId, Date dateBegin, Date dateEnd,
			String courseName, Long liveRoomId, Long teacherId) {
		List<CourseLive> courseLives = courseLiveRepository.findAll(new Specification<CourseLive>() {
			@Override
			public Predicate toPredicate(Root<CourseLive> root, CriteriaQuery<?> criteriaQuery,
					CriteriaBuilder criteriaBuilder) {
				criteriaQuery.distinct(true);

				List<Predicate> predicates = new ArrayList<>();

				Join<CourseLive, Course> course = root.join("course");
				predicates.add(criteriaBuilder.equal(course.get("status"), CourseStatus.WC));
				if (examType != null) {
					predicates.add(criteriaBuilder.equal(course.get("examType"), examType));
				}

				if (subjectId != null) {
					predicates.add(criteriaBuilder.equal(course.get("subjectId"), subjectId));
				}

				if (dateBegin != null) {
					predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("date"), dateBegin));
				}

				if (dateEnd != null) {
					predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("date"), dateEnd));
				}

				if (teacherId != null) {
					Join<CourseLive, CourseLiveTeacher> courseLiveTeachers = root.join("courseLiveTeachers");

					predicates.add(criteriaBuilder.equal(courseLiveTeachers.get("teacherId"), teacherId));
				}

				if (!Strings.isNullOrEmpty(courseName)) {
					predicates.add(criteriaBuilder.like(course.get("name"), "%" + courseName + "%"));
				}

				if (liveRoomId != null) {
					predicates.add(criteriaBuilder.equal(root.get("liveRoomId"), liveRoomId));
				}

				return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		});

		// 直播
		List<CourseLiveScheduleVo> courseLiveScheduleVos = Lists.newArrayList();

		// 直播字典
		Map<Long, CourseLiveScheduleVo> courseLiveDic = Maps.newHashMap();

		courseLives.forEach(courseLive -> {
			CourseLiveScheduleVo courseLiveScheduleVo = new CourseLiveScheduleVo();

			courseLiveScheduleVo.setId(courseLive.getId());
			courseLiveScheduleVo.setDate(
					DateformatUtil.format0(courseLive.getDate()) );
			courseLiveScheduleVo.setTimeBegin(TimeformatUtil.format(courseLive.getTimeBegin()));
			courseLiveScheduleVo.setTimeEnd(TimeformatUtil.format(courseLive.getTimeEnd()));
			courseLiveScheduleVo.setCourseName(courseLive.getCourse().getName());
			courseLiveScheduleVo.setCategoryName(courseLive.getCourse().getCourseCategory().getText());
			courseLiveScheduleVo.setPlace(courseLive.getCourse().getPlace());
			if (courseLive.getCourse().getAssistantFlag()) {
				if (courseLive.getAssistant() != null) {
					courseLiveScheduleVo.setAssistantName(courseLive.getAssistant().getName());
				}
			}

			if (courseLive.getCourse().getControllerFlag()) {
				if (courseLive.getController() != null) {
					courseLiveScheduleVo.setControllerName(courseLive.getController().getName());
				}
			}

			if (courseLive.getCourse().getCompereFlag()) {
				if (courseLive.getCompere() != null) {
					courseLiveScheduleVo.setCompereName(courseLive.getCompere().getName());
				}
			}

			if (courseLive.getCourse().getLearningTeacherFlag()) {
				if (courseLive.getLearningTeacher() != null) {
					courseLiveScheduleVo.setLearningTeacherName(courseLive.getLearningTeacher().getName());
				}
			}

			List<CourseLiveScheduleVo.TeacherInfo> teacherInfos = Lists.newArrayList();

			courseLive.getCourseLiveTeachers().forEach(courseLiveTeacher -> {
				if (courseLiveTeacher.getTeacherId().equals(teacherId)) {
					CourseLiveScheduleVo.TeacherInfo teacherInfo = new CourseLiveScheduleVo.TeacherInfo();
					if (courseLiveTeacher.getCoursePhase() != null) {
						teacherInfo.setPhase(courseLiveTeacher.getCoursePhase().getText());
					}

					if (courseLiveTeacher.getModule() != null) {
						teacherInfo.setModel(courseLiveTeacher.getModule().getName());
					}
					if (courseLiveTeacher.getSubject() != null) {
						teacherInfo.setSubject(courseLiveTeacher.getSubject().getName());
					}

					if (courseLiveTeacher.getTeacher() != null) {
						teacherInfo.setName(courseLiveTeacher.getTeacher().getName());
					}

					teacherInfos.add(teacherInfo);
				}
			});

			courseLiveScheduleVo.setTeacherInfos(teacherInfos);

			courseLiveScheduleVo.setExamType(courseLive.getCourse().getExamType().getText());

			courseLiveScheduleVo.setCourseLiveName(courseLive.getName());

			Subject subject = courseLive.getCourse().getSubject();
			if (subject != null) {
				courseLiveScheduleVo.setSubject(subject.getName());
			}

			courseLiveScheduleVo.setSourceId(courseLive.getSourceId());

			courseLiveScheduleVos.add(courseLiveScheduleVo);

			courseLiveDic.put(courseLive.getId(), courseLiveScheduleVo);
		});

		// 处理滚动排课
		Iterator<CourseLiveScheduleVo> courseLiveScheduleVoIterator = courseLiveScheduleVos.iterator();
		while (courseLiveScheduleVoIterator.hasNext()) {
			CourseLiveScheduleVo temp = courseLiveScheduleVoIterator.next();
			// 如果是滚动排课，则只在源课程中追加课程名称
			if (temp.getSourceId() != null) {
				CourseLiveScheduleVo sourceCourseLive = courseLiveDic.get(temp.getSourceId());
				if (sourceCourseLive != null) {
					sourceCourseLive.setCourseName(sourceCourseLive.getCourseName() + "，" + temp.getCourseName());

					courseLiveScheduleVoIterator.remove();
				}
			}
		}

		return courseLiveScheduleVos;
	}

	@Override
	public List<CourseLive> findByCourseIdAndDateInAndTimeBeginAndTimeEnd(Long courseId, List<Date> dates, Integer timeBegin,
			Integer timeEnd) {
		return courseLiveRepository.findByCourseIdAndDateInAndTimeBeginAndTimeEnd(courseId, dates, timeBegin, timeEnd);
	}

	@Override
	public Long getCourseSubjectId(Long courseLiveId) {
		return courseRepository.getCourseSubjectId(courseLiveId);
	}

	@Override
	public List<CourseLive> findByDateAndTeacherId(Date dateBegin, Date dateEnd, Long teacherId,Pageable page){
		List<Sort.Order> list=new ArrayList();
		list.add(new Sort.Order(Sort.Direction.ASC, "dateInt"));//日期
		list.add(new Sort.Order(Sort.Direction.ASC, "timeBegin"));//开始时间
		list.add(new Sort.Order(Sort.Direction.ASC, "timeEnd"));//结束时间
        List<CourseLive> courseLives = courseLiveRepository.findAll(new Specification<CourseLive>() {
            @Override
            public Predicate toPredicate(Root<CourseLive> root, CriteriaQuery<?> criteriaQuery,
                                         CriteriaBuilder criteriaBuilder) {
                criteriaQuery.distinct(true);

                List<Predicate> predicates = new ArrayList<>();

                Join<CourseLive, Course> course = root.join("course");
                predicates.add(criteriaBuilder.equal(course.get("status"), CourseStatus.WC));//完成状态
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("date"), dateBegin));//开始日期
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("date"), dateEnd));//完成日期
                predicates.add(criteriaBuilder.isNull(root.get("sourceId")));//不为滚动排课
                if (teacherId != null) {
                    predicates.add(criteriaBuilder.equal(root.join("courseLiveTeachers", JoinType.LEFT).get("teacherId"), teacherId));
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        }, new Sort(list));
        return courseLives;
	}

	@Override
	public int updateCourseLiveCategoryByStatus(Long liveId, CourseLiveCategory courseLiveCategory) {
		return courseLiveRepository.updateCourseLiveCategoryByStatus(liveId,courseLiveCategory);
	}

	@Override
	public List<TaskLiveDGTVo> getLiveByDGT() {
	    List<TaskLiveDGTVo> taskVos=new ArrayList();
        List<CourseLive> taskDGTByAss = courseLiveRepository.getTaskDGTByAss();//助教
        taskDGTByAss.forEach(task->{
            TaskLiveDGTVo vo=new TaskLiveDGTVo();
            vo.setRole(TeacherType.ZJ);
            if(task.getAssistant()!=null){
                vo.setTeacherName(task.getAssistant().getName());
            }
            vo.setLiveId(task.getId());
            vo.setLiveName(task.getName());
            vo.setCourseId(task.getCourseId());
            vo.setCourseName(task.getCourse().getName());
            Integer begin = task.getTimeBegin();
            Integer end = task.getTimeEnd();
            if(null!=begin&&null!=end){
                String timeBegin = TimeRangeUtil.intToDateString(begin);
                String timeEnd = TimeRangeUtil.intToDateString(end);
                vo.setTimeRange(task.getDate().toString()+" "+timeBegin+"-"+timeEnd);
            }
            vo.setConfirmKey(task.getAssConfirm().getValue());
            vo.setConfirmStatus(task.getAssConfirm().getText());
            taskVos.add(vo);

        });
        List<CourseLive> taskDGTByCtrl = courseLiveRepository.getTaskDGTByCtrl();//场控
        taskDGTByCtrl.forEach(task->{
            TaskLiveDGTVo vo=new TaskLiveDGTVo();
            vo.setRole(TeacherType.CK);
            if(task.getController()!=null){
                vo.setTeacherName(task.getController().getName());
            }
            vo.setLiveId(task.getId());
            vo.setLiveName(task.getName());
            vo.setCourseId(task.getCourseId());
            vo.setCourseName(task.getCourse().getName());
            Integer begin = task.getTimeBegin();
            Integer end = task.getTimeEnd();
            if(null!=begin&&null!=end){
                String timeBegin = TimeRangeUtil.intToDateString(begin);
                String timeEnd = TimeRangeUtil.intToDateString(end);
                vo.setTimeRange(task.getDate().toString()+" "+timeBegin+"-"+timeEnd);
            }
            vo.setConfirmKey(task.getCtrlConfirm().getValue());
            vo.setConfirmStatus(task.getCtrlConfirm().getText());
            taskVos.add(vo);

        });
        List<CourseLive> taskDGTByCom = courseLiveRepository.getTaskDGTByCom();//主持人
        taskDGTByCom.forEach(task->{
            TaskLiveDGTVo vo=new TaskLiveDGTVo();
            vo.setRole(TeacherType.ZCR);
            if(task.getCompere()!=null){
                vo.setTeacherName(task.getCompere().getName());
            }
            vo.setLiveId(task.getId());
            vo.setLiveName(task.getName());
            vo.setCourseId(task.getCourseId());
            vo.setCourseName(task.getCourse().getName());
            Integer begin = task.getTimeBegin();
            Integer end = task.getTimeEnd();
            if(null!=begin&&null!=end){
                String timeBegin = TimeRangeUtil.intToDateString(begin);
                String timeEnd = TimeRangeUtil.intToDateString(end);
                vo.setTimeRange(task.getDate().toString()+" "+timeBegin+"-"+timeEnd);
            }
            vo.setConfirmKey(task.getComConfirm().getValue());
            vo.setConfirmStatus(task.getComConfirm().getText());
            taskVos.add(vo);

        });
        List<CourseLive> taskDGTByLt = courseLiveRepository.getTaskDGTByLt();//学习师
        taskDGTByLt.forEach(task->{
            TaskLiveDGTVo vo=new TaskLiveDGTVo();
            vo.setRole(TeacherType.XXS);
            if(task.getLearningTeacher()!=null){
                vo.setTeacherName(task.getLearningTeacher().getName());
            }
            vo.setLiveId(task.getId());
            vo.setLiveName(task.getName());
            vo.setCourseId(task.getCourseId());
            vo.setCourseName(task.getCourse().getName());
            Integer begin = task.getTimeBegin();
            Integer end = task.getTimeEnd();
            if(null!=begin&&null!=end){
                String timeBegin = TimeRangeUtil.intToDateString(begin);
                String timeEnd = TimeRangeUtil.intToDateString(end);
                vo.setTimeRange(task.getDate().toString()+" "+timeBegin+"-"+timeEnd);
            }
            vo.setConfirmKey(task.getLtConfirm().getValue());
            vo.setConfirmStatus(task.getLtConfirm().getText());
            taskVos.add(vo);

        });
        List<CourseLiveTeacher> taskDGT = courseLiveTeacherRepository.getTaskDGT();//教师
        taskDGT.forEach(task->{
            TaskLiveDGTVo vo=new TaskLiveDGTVo();
            vo.setRole(TeacherType.JS);
            if(task.getTeacher()!=null){
                vo.setTeacherName(task.getTeacher().getName());
            }
            vo.setLiveId(task.getCourseLive().getId());
            vo.setLiveName(task.getCourseLive().getName());
            vo.setCourseId(task.getCourseLive().getCourseId());
            vo.setCourseName(task.getCourseLive().getCourse().getName());
            Integer begin = task.getCourseLive().getTimeBegin();
            Integer end = task.getCourseLive().getTimeEnd();
            if(null!=begin&&null!=end){
                String timeBegin = TimeRangeUtil.intToDateString(begin);
                String timeEnd = TimeRangeUtil.intToDateString(end);
                vo.setTimeRange(task.getCourseLive().getDate().toString()+" "+timeBegin+"-"+timeEnd);
            }
            vo.setConfirmKey(task.getConfirm().getValue());
            vo.setConfirmStatus(task.getConfirm().getText());
            vo.setLiveTeacherId(task.getId());
            taskVos.add(vo);
        });
        return taskVos;
    }

    @Override
    @Transactional
    public void submitCourseLiveTeacherDGT(Long liveId, Long liveITeacherd, TeacherType teacherType,
                                           Long teacherId, TeacherCourseLevel level) {
        Teacher oldTeacher=null;
        String oldPhone =null;
        Teacher newTeacher=null;
        String newPhone =null;
        CourseLive live=courseLiveRepository.findOne(liveId);
        CourseLiveTeacher data=null;
        switch (teacherType){
            case JS:
                if(liveITeacherd==null){
                    throw new BadRequestException("直播教师数据id不能为空");
                }
                data = courseLiveTeacherRepository.findOne(liveITeacherd);//查找数据
                data.setConfirm(CourseConfirmStatus.DQR);
                if(null!=level){
                    data.setTeacherCourseLevel(level);//更改级别
                }
                oldTeacher=data.getTeacher();
                oldPhone=oldTeacher.getPhone();//取出原教师电话
                newTeacher = teacherService.findOne(teacherId);
                newPhone = newTeacher.getPhone();//新教师电话
                data.setTeacherId(teacherId);//更改教师
                courseLiveTeacherRepository.save(data);//回写数据
                confirmTokenRepository.updateExpire(liveId,liveITeacherd,TeacherType.JS.ordinal());//使token过期
                break;
            case ZJ:
                oldTeacher=live.getAssistant();
                oldPhone=oldTeacher.getPhone();
				live.setAssConfirm(CourseConfirmStatus.DQR);
				newTeacher = teacherService.findOne(teacherId);
                newPhone = newTeacher.getPhone();
                live.setAssistantId(teacherId);
                courseLiveRepository.save(live);
                confirmTokenRepository.updateExpireZJ(liveId,TeacherType.ZJ.ordinal());
                break;
            case ZCR:
                oldTeacher=live.getCompere();
                oldPhone=oldTeacher.getPhone();
				live.setComConfirm(CourseConfirmStatus.DQR);
                newTeacher = teacherService.findOne(teacherId);
                newPhone = newTeacher.getPhone();
                live.setCompereId(teacherId);
                courseLiveRepository.save(live);
                confirmTokenRepository.updateExpireZJ(liveId,TeacherType.ZCR.ordinal());
                break;
            case CK:
                oldTeacher=live.getController();
                oldPhone=oldTeacher.getPhone();
				live.setCtrlConfirm(CourseConfirmStatus.DQR);
                newTeacher = teacherService.findOne(teacherId);
                newPhone = newTeacher.getPhone();
                live.setControllerId(teacherId);
                courseLiveRepository.save(live);
                confirmTokenRepository.updateExpireZJ(liveId,TeacherType.CK.ordinal());
                break;
            case XXS:
                oldTeacher=live.getLearningTeacher();
                oldPhone=oldTeacher.getPhone();
				live.setLtConfirm(CourseConfirmStatus.DQR);
                newTeacher = teacherService.findOne(teacherId);
                newPhone = newTeacher.getPhone();
                live.setLearningTeacherId(teacherId);
                courseLiveRepository.save(live);
                confirmTokenRepository.updateExpireZJ(liveId,TeacherType.XXS.ordinal());
                break;
        }

        if(!oldPhone.equals(newPhone)){
            //发送短信通知
            StringBuffer sb=new StringBuffer();
            sb.append("您沟通的课程:");
            sb.append(live.getCourse().getName());
            sb.append(",于");
            sb.append(live.getDate().toString()+" "+TimeRangeUtil.intToDateString(live.getTimeBegin())+"-"+TimeRangeUtil.intToDateString(live.getTimeEnd()));
            sb.append(" 安排的直播:");
            sb.append(live.getName());
            sb.append(",已重新进行安排");
            SmsUtil.sendSms(oldPhone,sb.toString());
            log.info("发送取消排课短信 : {} -> {}", newPhone, sb.toString());
        }

        Course course=live.getCourse();//课程
        StringBuilder content = new StringBuilder("直播确认:");
        ConfirmToken confirmToken = new ConfirmToken();
        confirmToken.setTeacherId(newTeacher.getId());
        confirmToken.setSourseId(liveId);
        confirmToken.setTeacherType(teacherType);
        if(TeacherType.JS.equals(teacherType)){//教师类型
            confirmToken.setCourseLiveTeacherId(liveITeacherd);
        }
        confirmToken.setToken(UUID.randomUUID().toString().replaceAll("-", ""));
        confirmTokenRepository.save(confirmToken);

        StringBuilder currentLiveContent = new StringBuilder();
        currentLiveContent.append(course.getName()).append("-").append(live.getName()).append("-")
                .append(DateformatUtil.format0(live.getDate())).append(" ")
                .append(TimeformatUtil.format(live.getTimeBegin())).append("-")
                .append(TimeformatUtil.format(live.getTimeEnd())).append(" ")
                .append(apiHost + "/mobile/confirm.html?token=" + confirmToken.getToken()).append(" ");
        content.append(currentLiveContent);
        SmsUtil.sendSms(newPhone, content.toString());
        log.info("发送教师确认短信 : {} -> {}", newPhone, content.toString());
    }
}
