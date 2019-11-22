package com.huatu.tiku.schedule.biz.service.imple;


import java.util.*;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.huatu.tiku.schedule.base.exception.BadRequestException;
import com.huatu.tiku.schedule.biz.domain.delete.DeleteCourseDetail;
import com.huatu.tiku.schedule.biz.enums.*;
import com.huatu.tiku.schedule.biz.repository.DeleteCourseDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.schedule.base.service.impl.BaseServiceImpl;
import com.huatu.tiku.schedule.biz.domain.*;
import com.huatu.tiku.schedule.biz.repository.ConfirmTokenRepository;
import com.huatu.tiku.schedule.biz.repository.CourseLiveRepository;
import com.huatu.tiku.schedule.biz.repository.CourseRepository;
import com.huatu.tiku.schedule.biz.service.CourseService;
import com.huatu.tiku.schedule.biz.util.DateformatUtil;
import com.huatu.tiku.schedule.biz.util.SmsUtil;
import com.huatu.tiku.schedule.biz.util.TimeformatUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CourseServiceImpl extends BaseServiceImpl<Course, Long> implements CourseService {

	private final CourseRepository courseRepository;

	private final DeleteCourseDetailRepository deleteCourseDetailRepository;

	private final CourseLiveRepository courseLiveRepository;

	private final ConfirmTokenRepository confirmTokenRepository;


	@Value("${api.host}")
	private String apiHost;

	@Autowired
	public CourseServiceImpl(CourseRepository courseRepository, DeleteCourseDetailRepository deleteCourseDetailRepository,
                             CourseLiveRepository courseLiveRepository,
			ConfirmTokenRepository confirmTokenRepository ) {
		this.courseRepository = courseRepository;
        this.deleteCourseDetailRepository= deleteCourseDetailRepository;
		this.courseLiveRepository = courseLiveRepository;
		this.confirmTokenRepository = confirmTokenRepository;
	}

	@Override
	public Page<Course> getCourseList(ExamType examType, String name, Long id, Long subjectId, Date dateBegin,
                                      Date dateEnd, String teacherName, CourseStatus status,Pageable page) {
		Specification<Course> querySpecific = new Specification<Course>() {
			@Override
			public Predicate toPredicate(Root<Course> root, CriteriaQuery<?> criteriaQuery,
					CriteriaBuilder criteriaBuilder) {
				criteriaQuery.distinct(true);

				if (!Long.class.equals(criteriaQuery.getResultType())) {
					root.fetch("subject", JoinType.LEFT);
				}

				List<Predicate> predicates = new ArrayList<>();

				if (examType != null) {
					predicates.add(criteriaBuilder.equal(root.get("examType"), examType));
				}
//				else{
//                    predicates.add(criteriaBuilder.notEqual(root.get("examType"), ExamType.MS));
//				}
				if (subjectId != null) {
					predicates.add(criteriaBuilder.equal(root.get("subject"), subjectId));
				}
				if (!Strings.isNullOrEmpty(name)) {
					predicates.add(criteriaBuilder.like(root.get("name"), "%" + name + "%"));
				}
				if (id != null) {
					predicates.add(criteriaBuilder.equal(root.get("id"), id));
				}
				Join<Course, CourseLive> courseLives = null;
				if (dateBegin != null && dateEnd != null) {
					courseLives = root.join("courseLives");

					predicates.add(criteriaBuilder.between(courseLives.get("date"), dateBegin, dateEnd));
				}
				if (!Strings.isNullOrEmpty(teacherName)) {
					if (courseLives == null) {
						courseLives = root.join("courseLives");
					}

					Join<CourseLive, CourseLiveTeacher> courseLiveTeachers = courseLives.join("courseLiveTeachers");

					Join<CourseLiveTeacher, Teacher> teachers = courseLiveTeachers.join("teacher");

					predicates.add(criteriaBuilder.like(teachers.get("name"), "%" + teacherName + "%"));
				}
				if(status!=null){
                    predicates.add(criteriaBuilder.equal(root.get("status"), status));
                }
				return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		};
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable=new PageRequest(page.getPageNumber(),page.getPageSize(),sort);
		return courseRepository.findAll(querySpecific, pageable);
	}

	@Override
	public Page<Course> getCourseList(List<ExamType> examTypes, String name, Long id, Long subjectId, Date dateBegin,
                                      Date dateEnd, String teacherName, CourseStatus status,Pageable page) {
		Specification<Course> querySpecific = new Specification<Course>() {
			@Override
			public Predicate toPredicate(Root<Course> root, CriteriaQuery<?> criteriaQuery,
					CriteriaBuilder criteriaBuilder) {
				criteriaQuery.distinct(true);

				if (!Long.class.equals(criteriaQuery.getResultType())) {
					root.fetch("subject", JoinType.LEFT);
				}

				List<Predicate> predicates = new ArrayList<>();

				if (examTypes != null && !examTypes.isEmpty()) {
					predicates.add(root.get("examType").in(examTypes));
				}
				if (subjectId != null) {
					predicates.add(criteriaBuilder.equal(root.get("subject"), subjectId));
				}
				if (!Strings.isNullOrEmpty(name)) {
					predicates.add(criteriaBuilder.like(root.get("name"), "%" + name + "%"));
				}
				if (id != null) {
					predicates.add(criteriaBuilder.equal(root.get("id"), id));
				}
				Join<Course, CourseLive> courseLives = null;
				if (dateBegin != null && dateEnd != null) {
					courseLives = root.join("courseLives");

					predicates.add(criteriaBuilder.between(courseLives.get("date"), dateBegin, dateEnd));
				}
				if (!Strings.isNullOrEmpty(teacherName)) {
					if (courseLives == null) {
						courseLives = root.join("courseLives");
					}

					Join<CourseLive, CourseLiveTeacher> courseLiveTeachers = courseLives.join("courseLiveTeachers");

					Join<CourseLiveTeacher, Teacher> teachers = courseLiveTeachers.join("teacher");

					predicates.add(criteriaBuilder.like(teachers.get("name"), "%" + teacherName + "%"));
				}
				if(status!=null){
                    predicates.add(criteriaBuilder.equal(root.get("status"), status));
                }
				return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		};
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable=new PageRequest(page.getPageNumber(),page.getPageSize(),sort);
		return courseRepository.findAll(querySpecific, pageable);
	}

	@Override
	@Transactional
	public Map<String, Object> submitCourseLive(Long id) {
		// 返回结果
		Map<String, Object> result = Maps.newHashMap();

		// 错误信息
		StringBuilder message = new StringBuilder();

		// 获取课程
		Course course =	courseRepository.findOne(id);

		if(course == null) {
			message.append("课程不存在");
		} else {
			// 如果状态为直播安排
			if(CourseStatus.ZBAP.equals(course.getStatus())) {
				List<CourseLive> courseLives = course.getCourseLives();

				if(courseLives.isEmpty()) {
					message.append("未添加直播");
				} else {
					// 未添加教师的直播
					List<CourseLive> emptyCourseLives = Lists.newArrayList();

					for (CourseLive courseLive : courseLives) {
						List<CourseLiveTeacher> courseLiveTeachers = courseLive.getCourseLiveTeachers();

						if(courseLiveTeachers.isEmpty()) {
							emptyCourseLives.add(courseLive);
						}
						else {
//							Optional<CourseLiveTeacher> courseLiveTeacher =	courseLiveTeachers.stream().filter(courseLiveTeacherTemp -> courseLiveTeacherTemp.getSubjectId() == null).findFirst();
//
//							// 必填项为空
//							if(courseLiveTeacher.isPresent()) {
//								message.append("科目为必填项");
//								break;
//							}
						}
					}

					if(message.length() == 0) {
						if(emptyCourseLives.size() == courseLives.size()) {
							message.append("未添加直播安排");
						} else {
							// 删除无效直播
							courseLiveRepository.delete(emptyCourseLives);

							// 更新课程状态
							course.setStatus(CourseStatus.JSAP);
							
							courseRepository.save(course);
						}
					}
				}
			} else {
				message.append("课程状态错误");
			}
		}

		if(message.length() == 0) {
			result.put("success", true);
			result.put("message", "提交成功");
		} else {
			result.put("success", false);
			result.put("message", message.toString());
		}

		return result;
	}

	@Override
	public List<Course> rollingCourse(Long id, List<Date> dates) {
		return courseRepository.findAll(new Specification<Course>() {
			@Override
			public Predicate toPredicate(Root<Course> root, CriteriaQuery<?> criteriaQuery,
					CriteriaBuilder criteriaBuilder) {
				criteriaQuery.distinct(true);

				List<Predicate> predicates = new ArrayList<>();

				predicates.add(criteriaBuilder.notEqual(root.get("id"), id));

				predicates.add(criteriaBuilder.equal(root.get("status"), CourseStatus.WC));//完成状态

				predicates.add(criteriaBuilder.equal(root.get("courseCategory"), CourseCategory.LIVE));//直播课程

				Join<Course, CourseLive> courseLives = root.join("courseLives");

				if (dates != null && dates.size() > 0) {
					predicates.add(courseLives.get("date").in(dates));
				}

				return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		});
	}

	@Override
	public Map<String, Object> submitCourseLiveTeacher(Long id) {
		// 返回结果
		Map<String, Object> result = Maps.newHashMap();

		// 错误信息
		StringBuilder message = new StringBuilder();

		// 获取课程
		Course course = courseRepository.findOne(id);

		if (course == null) {
			message.append("课程不存在");
		} else {
			// 检测课程状态
			if (course.getStatus().ordinal() >= CourseStatus.JSAP.ordinal()) {
				// 课程直播列表
				List<CourseLive> courseLives = course.getCourseLives();

				if (courseLives.isEmpty()) {
					message.append("未添加直播");
				} else {
					// 未添加教师的直播
					List<CourseLive> emptyCourseLives = Lists.newArrayList();
					boolean flag=false;
					for (CourseLive courseLive : courseLives) {
						if (message.length() == 0) {
							// 校验直播内容
							if (Strings.isNullOrEmpty(courseLive.getName())) {
								message.append("授课内容为必填项");
								break;
								// 校验直播间
							}
//							if(ExamType.MS.equals(course.getExamType())&&null==courseLive.getCourseLiveCategory()&& CourseCategory.XXK.equals(course.getCourseCategory())){//面试类型 授课类型没填时 课程类型为线下
							if(ExamType.MS.equals(course.getExamType())&&null==courseLive.getCourseLiveCategory()){//面试类型 授课类型没填时
								message.append("面试授课类型为必填项");
								break;
							}

							else {
								List<CourseLiveTeacher> courseLiveTeachers = courseLive.getCourseLiveTeachers();
								
								if (courseLiveTeachers.isEmpty()) {
									emptyCourseLives.add(courseLive);
								} else {
									for (CourseLiveTeacher courseLiveTeacher : courseLiveTeachers) {
										// 校验必填项
										if (courseLiveTeacher.getTeacherId() == null) {
											message.append("授课教师为必填项");
											break;
										}
										else if (courseLiveTeacher.getSubjectId() == null) {
											message.append("科目为必填项");
											break;
										}
										//courseLiveTeacher.setConfirm(CourseConfirmStatus.DQR);
									}
								}
								if(null==courseLive.getSourceId()){//如果不是滚动排课
									flag=true;
								}
							}
						}
					}

					if (message.length() == 0) {
						if (emptyCourseLives.size() == courseLives.size()) {
							message.append("未添加课程安排");
						} else {
							// 删除无效直播
							courseLiveRepository.delete(emptyCourseLives);
                            if (course.getStatus().equals(CourseStatus.JSAP)) {
                                // 更新课程状态
								if(flag){//有一个不是滚动排课的就是到教师确认
									course.setStatus(CourseStatus.JSQR);
								}else{//全是滚动排课 课程状态到完成
									course.setStatus(CourseStatus.WC);
								}
                                courseRepository.save(course);
                            }
						}
					}
				}
			} else {
				message.append("课程状态错误");
			}
		}

		if (message.length() == 0) {
			result.put("success", true);
			result.put("message", "提交成功");
		} else {
			result.put("success", false);
			result.put("message", message.toString());
		}

		return result;
	}

	@Override
	public Map<String, Object> submitCourseLiveAssitant(Long id) {
		// 返回结果
		Map<String, Object> result = Maps.newHashMap();

		// 错误信息
		StringBuilder message = new StringBuilder();

		// 获取课程
		Course course = courseRepository.findOne(id);

		if (course == null) {
			message.append("课程不存在");
		} else {
			// 检测课程状态
			if (CourseStatus.ZJAP.equals(course.getStatus())) {
				// 课程直播列表
				List<CourseLive> courseLives = course.getCourseLives();

				for (CourseLive courseLive : courseLives) {
					if (course.getAssistantFlag()) {
						if (courseLive.getAssistantId() == null) {
							message.append("助教为必填项");
							break;
						}
					}
					if (course.getControllerFlag()) {
						if (courseLive.getControllerId() == null) {
							message.append("场控为必填项");
							break;
						}
					}
					if (course.getCompereFlag()) {
						if (courseLive.getCompereId() == null) {
							message.append("主持人为必填项");
							break;
						}
					}
					if (course.getLearningTeacherFlag()) {
						if (courseLive.getLearningTeacherId() == null) {
							message.append("学习师为必填项");
							break;
						}
					}
				}
				if (message.length() == 0) {
					// 更新课程状态
					course.setStatus(CourseStatus.ZJQR);

					courseRepository.save(course);
				}
			} else {
				message.append("课程状态错误");
			}
		}

		if (message.length() == 0) {
			result.put("success", true);
			result.put("message", "提交成功");
		} else {
			result.put("success", false);
			result.put("message", message.toString());
		}

		return result;
	}

    @Override
    public List<Course> findAllByLives(List<Long> liveIds) {
        return courseRepository.findAllByLives(liveIds);
    }

    @Override
    public void updateStatus(Course course, TeacherType teacherType) {
        Boolean assistantFlag = course.getAssistantFlag();
        Boolean compereFlag = course.getCompereFlag();
        Boolean controllerFlag = course.getControllerFlag();
        Boolean learningTeacherFlag = course.getLearningTeacherFlag();
        List<CourseLive> courseLives = course.getCourseLives();
        for(CourseLive courseLive:courseLives){//循环直播
            if(!TeacherType.JS.equals(teacherType)){//助教类型 判断助教是否确认
                if (assistantFlag) {//如果需要助教
                    if (!CourseConfirmStatus.QR.equals(courseLive.getAssConfirm())&&courseLive.getSourceId()==null) {//如果助教未确认且不是滚动排课
                        return;
                    }
                }
                if (compereFlag) {
                    if (!CourseConfirmStatus.QR.equals(courseLive.getComConfirm())&&courseLive.getSourceId()==null) {//如果主持人未确认且不是滚动排课
                        return;
                    }
                }
                if (controllerFlag) {
                    if (!CourseConfirmStatus.QR.equals(courseLive.getCtrlConfirm())&&courseLive.getSourceId()==null) {//如果控场未确认且不是滚动排课
                        return;
                    }
                }
                if (learningTeacherFlag) {
                    if (!CourseConfirmStatus.QR.equals(courseLive.getLtConfirm())&&courseLive.getSourceId()==null) {//如果学习师未确认且不是滚动排课
                        return;
                    }
                }
            }else{//教师类型 判断直播教师是否确认
                List<CourseLiveTeacher> courseLiveTeachers = courseLive.getCourseLiveTeachers();
                for(CourseLiveTeacher courseLiveTeacher:courseLiveTeachers){
                    if(!CourseConfirmStatus.QR.equals(courseLiveTeacher.getConfirm())){//如果未确认 直接返回
                        return ;
                    }
                }
            }

        }

        if(!TeacherType.JS.equals(teacherType)){//如果是助教
            course.setStatus(CourseStatus.WC);//状态改为完成
        }else{//否则是讲师
            if(!assistantFlag&&!compereFlag&&!controllerFlag&&!learningTeacherFlag){//如果助教全没选
                course.setStatus(CourseStatus.WC);//状态改为完成
            }else {
				//如果助教全部确认 是教务更改教师的情况 状态改为完成
				//助教没全部确认 是正常流程 或者 是添加了直播 将状态改成助教安排
				CourseStatus status=CourseStatus.WC;
				for(CourseLive courseLive:courseLives) {//循环直播
					if (assistantFlag) {//如果需要助教
						if (!CourseConfirmStatus.QR.equals(courseLive.getAssConfirm())&&courseLive.getSourceId()==null) {//如果助教未确认且不是滚动排课
							status=CourseStatus.ZJAP;//助教安排状态
						}
					}
					if (compereFlag) {
						if (!CourseConfirmStatus.QR.equals(courseLive.getComConfirm())&&courseLive.getSourceId()==null) {//如果主持人未确认且不是滚动排课
							status=CourseStatus.ZJAP;
						}
					}
					if (controllerFlag) {
						if (!CourseConfirmStatus.QR.equals(courseLive.getCtrlConfirm())&&courseLive.getSourceId()==null) {//如果控场未确认且不是滚动排课
							status=CourseStatus.ZJAP;
						}
					}
					if (learningTeacherFlag) {
						if (!CourseConfirmStatus.QR.equals(courseLive.getLtConfirm())&&courseLive.getSourceId()==null) {//如果学习师未确认且不是滚动排课
							status=CourseStatus.ZJAP;
						}
					}
				}
                course.setStatus(status);//状态更改
            }
        }
        courseRepository.save(course);
    }

	@Override
	@Transactional
	public void sendCourseLiveTeacherConfirmSms(Long id) {
		Course course = courseRepository.findOne(id);

		// 需要发送的数据<手机号，信息内容>
		Map<String, List<StringBuilder>> datas = Maps.newHashMap();

        List<CourseLive> courseLives = course.getCourseLives();//课程直播集合
        //对直播排序
        courseLives.sort((o1, o2) -> {
            int numDate=o1.getDateInt()-o2.getDateInt();
            if(numDate==0){//日期相等 判断开始时间
                int numTime=o1.getTimeBegin()-o2.getTimeBegin();
                return numTime==0?o1.getTimeEnd()-o2.getTimeEnd():numTime;
            }else{
                return numDate;
            }
        });
        courseLives.forEach(courseLive -> {
			courseLive.getCourseLiveTeachers().forEach(courseLiveTeacher -> {
				if (CourseConfirmStatus.DQR.equals(courseLiveTeacher.getConfirm())) {
					Teacher teacher = courseLiveTeacher.getTeacher();
					String phone = teacher.getPhone();

					// 手机号-短信内容
					List<StringBuilder> contents = datas.get(phone);

					// 短信内容
					StringBuilder content = null;

					if (contents == null) {
						contents = Lists.newArrayList();

						content = new StringBuilder("直播确认:");

						contents.add(content);

						datas.put(phone, contents);
					} else {
						content = contents.get(contents.size() - 1);
					}

					ConfirmToken confirmToken = new ConfirmToken();
					confirmToken.setTeacherId(teacher.getId());
					confirmToken.setSourseId(courseLive.getId());
					confirmToken.setTeacherType(teacher.getTeacherType());
					confirmToken.setCourseLiveTeacherId(courseLiveTeacher.getId());
					confirmToken.setToken(UUID.randomUUID().toString().replaceAll("-", ""));

					confirmTokenRepository.save(confirmToken);
					// 课程名称-直播内容-日期 时间-地址;
					StringBuilder currentLiveContent = new StringBuilder();
					currentLiveContent.append(course.getName()).append("-").append(courseLive.getName()).append("-")
							.append(DateformatUtil.format0(courseLive.getDate())).append(" ")
							.append(TimeformatUtil.format(courseLive.getTimeBegin())).append("-")
							.append(TimeformatUtil.format(courseLive.getTimeEnd())).append(" ")
							.append(apiHost + "/mobile/confirm.html?token=" + confirmToken.getToken()).append(" ");

					// 检查短信长度
					if (SmsUtil.checkLength(content.toString(), currentLiveContent.toString())) {
						content.append(currentLiveContent);
					} else {
						StringBuilder contentNew = new StringBuilder("直播确认:");

						contentNew.append(currentLiveContent);

						contents.add(contentNew);
					}
				}
			});
		});

		// 发送信息
		datas.entrySet().forEach(data -> {
			data.getValue().forEach(sms -> {
				SmsUtil.sendSms(data.getKey(), sms.toString());
                log.info("发送教师确认短信 : {} -> {}", data.getKey(), sms.toString());
            });
		});
	}

	@Override
	@Transactional
	public void sendCourseLiveAssitantConfirmSms(Long id) {
		Course course = courseRepository.findOne(id);

		// 需要发送的数据<手机号，信息内容>
		Map<String, List<StringBuilder>> datas = Maps.newHashMap();
        List<CourseLive> courseLives = course.getCourseLives();//课程直播集合
        //对直播排序
        courseLives.sort((o1, o2) -> {
            int numDate=o1.getDateInt()-o2.getDateInt();
            if(numDate==0){//日期相等 判断开始时间
                int numTime=o1.getTimeBegin()-o2.getTimeBegin();
                return numTime==0?o1.getTimeEnd()-o2.getTimeEnd():numTime;
            }else{
                return numDate;
            }
        });

        courseLives.forEach(courseLive -> {
			// 学习师
			if (course.getLearningTeacherFlag() && CourseConfirmStatus.DQR.equals(courseLive.getLtConfirm())) {
				Teacher teacher = courseLive.getLearningTeacher();
				String phone = teacher.getPhone();

				// 手机号-短信内容
				List<StringBuilder> contents = datas.get(phone);

				// 短信内容
				StringBuilder content = null;

				if (contents == null) {
					contents = Lists.newArrayList();

					content = new StringBuilder("直播确认:");

					contents.add(content);

					datas.put(phone, contents);
				} else {
					content = contents.get(contents.size() - 1);
				}

				ConfirmToken confirmToken = new ConfirmToken();
				confirmToken.setTeacherId(teacher.getId());
				confirmToken.setSourseId(courseLive.getId());
				confirmToken.setTeacherType(teacher.getTeacherType());
				confirmToken.setToken(UUID.randomUUID().toString().replaceAll("-", ""));

				confirmTokenRepository.save(confirmToken);

				// 课程名称-直播内容-日期 时间-地址;
				StringBuilder currentLiveContent = new StringBuilder();
				currentLiveContent.append(course.getName()).append("-").append(courseLive.getName()).append("-")
						.append(DateformatUtil.format0(courseLive.getDate())).append(" ")
						.append(TimeformatUtil.format(courseLive.getTimeBegin())).append("-")
						.append(TimeformatUtil.format(courseLive.getTimeEnd())).append(" ")
						.append(apiHost + "/mobile/confirm.html?token=" + confirmToken.getToken()).append(" ");

				// 检查短信长度
				if (SmsUtil.checkLength(content.toString(), currentLiveContent.toString())) {
					content.append(currentLiveContent);
				} else {
					StringBuilder contentNew = new StringBuilder("直播确认:");

					contentNew.append(currentLiveContent);

					contents.add(contentNew);
				}
			}
			// 助教
			if (course.getAssistantFlag() && CourseConfirmStatus.DQR.equals(courseLive.getAssConfirm())) {
				Teacher teacher = courseLive.getAssistant();
				String phone = teacher.getPhone();

				// 手机号-短信内容
				List<StringBuilder> contents = datas.get(phone);

				// 短信内容
				StringBuilder content = null;

				if (contents == null) {
					contents = Lists.newArrayList();

					content = new StringBuilder("直播确认:");

					contents.add(content);

					datas.put(phone, contents);
				} else {
					content = contents.get(contents.size() - 1);
				}

				ConfirmToken confirmToken = new ConfirmToken();
				confirmToken.setTeacherId(teacher.getId());
				confirmToken.setSourseId(courseLive.getId());
				confirmToken.setTeacherType(teacher.getTeacherType());
				confirmToken.setToken(UUID.randomUUID().toString().replaceAll("-", ""));

				confirmTokenRepository.save(confirmToken);

				// 课程名称-直播内容-日期 时间-地址;
				StringBuilder currentLiveContent = new StringBuilder();
				currentLiveContent.append(course.getName()).append("-").append(courseLive.getName()).append("-")
						.append(DateformatUtil.format0(courseLive.getDate())).append(" ")
						.append(TimeformatUtil.format(courseLive.getTimeBegin())).append("-")
						.append(TimeformatUtil.format(courseLive.getTimeEnd())).append(" ")
						.append(apiHost + "/mobile/confirm.html?token=" + confirmToken.getToken()).append(" ");

				// 检查短信长度
				if (SmsUtil.checkLength(content.toString(), currentLiveContent.toString())) {
					content.append(currentLiveContent);
				} else {
					StringBuilder contentNew = new StringBuilder("直播确认:");

					contentNew.append(currentLiveContent);

					contents.add(contentNew);
				}
			}
			// 场控
			if (course.getControllerFlag() && CourseConfirmStatus.DQR.equals(courseLive.getCtrlConfirm())) {
				Teacher teacher = courseLive.getController();
				String phone = teacher.getPhone();

				// 手机号-短信内容
				List<StringBuilder> contents = datas.get(phone);

				// 短信内容
				StringBuilder content = null;

				if (contents == null) {
					contents = Lists.newArrayList();

					content = new StringBuilder("直播确认:");

					contents.add(content);

					datas.put(phone, contents);
				} else {
					content = contents.get(contents.size() - 1);
				}

				ConfirmToken confirmToken = new ConfirmToken();
				confirmToken.setTeacherId(teacher.getId());
				confirmToken.setSourseId(courseLive.getId());
				confirmToken.setTeacherType(teacher.getTeacherType());
				confirmToken.setToken(UUID.randomUUID().toString().replaceAll("-", ""));

				confirmTokenRepository.save(confirmToken);

				// 课程名称-直播内容-日期 时间-地址;
				StringBuilder currentLiveContent = new StringBuilder();
				currentLiveContent.append(course.getName()).append("-").append(courseLive.getName()).append("-")
						.append(DateformatUtil.format0(courseLive.getDate())).append(" ")
						.append(TimeformatUtil.format(courseLive.getTimeBegin())).append("-")
						.append(TimeformatUtil.format(courseLive.getTimeEnd())).append(" ")
						.append(apiHost + "/mobile/confirm.html?token=" + confirmToken.getToken()).append(" ");

				// 检查短信长度
				if (SmsUtil.checkLength(content.toString(), currentLiveContent.toString())) {
					content.append(currentLiveContent);
				} else {
					StringBuilder contentNew = new StringBuilder("直播确认:");

					contentNew.append(currentLiveContent);

					contents.add(contentNew);
				}
			}
			// 主持人
			if (course.getCompereFlag() && CourseConfirmStatus.DQR.equals(courseLive.getComConfirm())) {
				Teacher teacher = courseLive.getCompere();
				String phone = teacher.getPhone();

				// 手机号-短信内容
				List<StringBuilder> contents = datas.get(phone);

				// 短信内容
				StringBuilder content = null;

				if (contents == null) {
					contents = Lists.newArrayList();

					content = new StringBuilder("直播确认:");

					contents.add(content);

					datas.put(phone, contents);
				} else {
					content = contents.get(contents.size() - 1);
				}

				ConfirmToken confirmToken = new ConfirmToken();
				confirmToken.setTeacherId(teacher.getId());
				confirmToken.setSourseId(courseLive.getId());
				confirmToken.setTeacherType(teacher.getTeacherType());
				confirmToken.setToken(UUID.randomUUID().toString().replaceAll("-", ""));

				confirmTokenRepository.save(confirmToken);

				// 课程名称-直播内容-日期 时间-地址;
				StringBuilder currentLiveContent = new StringBuilder();
				currentLiveContent.append(course.getName()).append("-").append(courseLive.getName()).append("-")
						.append(DateformatUtil.format0(courseLive.getDate())).append(" ")
						.append(TimeformatUtil.format(courseLive.getTimeBegin())).append("-")
						.append(TimeformatUtil.format(courseLive.getTimeEnd())).append(" ")
						.append(apiHost + "/mobile/confirm.html?token=" + confirmToken.getToken()).append(" ");

				// 检查短信长度
				if (SmsUtil.checkLength(content.toString(), currentLiveContent.toString())) {
					content.append(currentLiveContent);
				} else {
					StringBuilder contentNew = new StringBuilder("直播确认:");

					contentNew.append(currentLiveContent);

					contents.add(contentNew);
				}
			}
		});

		// 发送信息
		datas.entrySet().forEach(data -> {
			data.getValue().forEach(sms -> {
				log.info("发送助教确认短信 : {} -> {}", data.getKey(), sms.toString());
				SmsUtil.sendSms(data.getKey(), sms.toString());
			});
		});
	}

	@Override
	public void saveInterview(Long courseId,  List<Long> teacherIds) {
		// 添加权限
		teacherIds.forEach(teacherId -> {
			courseRepository.saveInterviewTeacherId(courseId, teacherId);
		});
	}

    @Override
    public Boolean cancelCourse(Long courseId) {
        return 0!= courseRepository.updateCourseStatusById(courseId,CourseStatus.JSAP.ordinal(),CourseStatus.ZBAP.ordinal());
    }

    @Override
    public void findCourseStatusByCourseId(Long courseId) {
        CourseStatus status = courseRepository.findCourseStatusByCourseId(courseId);
        if(status!=null&&status.equals(CourseStatus.ZBAP)){//如果直播安排状态 表示已经被运营撤销
            throw new BadRequestException("该课程已撤销,请勿操作");
        }
    }

	@Override
    @Transactional
	public void sendCourseDeleteSms(Course course, String reason) {
        CourseStatus status = course.getStatus();
        Set<String> phones=new HashSet<>();//存储手机号
		if(status.ordinal()>=2){//直播安排或教师安排直接删除
            Boolean assistantFlag = course.getAssistantFlag();//需要助教标志
            Boolean compereFlag = course.getCompereFlag();//需要主持人标志
            Boolean controllerFlag = course.getControllerFlag();//需要场控标志
            Boolean learningTeacherFlag = course.getLearningTeacherFlag();//需要学习师标志
            course.getCourseLives().forEach(live->{
                if(status.ordinal()>=4){//助教确认或者完成 给助教发短信
                    Teacher assistant = live.getAssistant();
                    if(assistantFlag&&assistant!=null){
                        phones.add(assistant.getPhone());//添加助教电话
                    }
                    Teacher compere = live.getCompere();
                    if(compereFlag&&compere!=null){
                        phones.add(compere.getPhone());//添加主持人电话
                    }
                    Teacher controller = live.getController();
                    if(controllerFlag&&controller!=null){
                        phones.add(controller.getPhone());//添加场控电话
                    }
                    Teacher learningTeacher = live.getLearningTeacher();
                    if(learningTeacherFlag&&learningTeacher!=null){
                        phones.add(learningTeacher.getPhone());//添加学习师电话
                    }
                }

                live.getCourseLiveTeachers().forEach(lt->{
                    Teacher teacher = lt.getTeacher();
                    if(teacher!=null){
                        phones.add(teacher.getPhone());//添加讲师电话
                    }
                });
            });
			List list=new ArrayList();
			list.addAll(phones);
			SmsUtil.sendMultipleSms(list,"\""+course.getName()+"\"该课程已删除,课程下直播已取消");
		}
        DeleteCourseDetail detail=new DeleteCourseDetail();
        detail.setCourseId(course.getId());
        detail.setStatus(course.getStatus());
        detail.setReason(reason);
        deleteCourseDetailRepository.save(detail);
        courseRepository.delete(course.getId());//删除课程
    }

}
