package com.huatu.tiku.schedule.biz.controller;

import static com.huatu.tiku.schedule.biz.util.TimeRangeUtil.intToDateString;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.IntStream;

import javax.validation.Valid;

import com.huatu.tiku.schedule.biz.domain.*;
import com.huatu.tiku.schedule.biz.dto.*;
import com.huatu.tiku.schedule.biz.service.CourseLiveTeacherService;
import com.huatu.tiku.schedule.biz.service.TeacherService;
import com.huatu.tiku.schedule.biz.service.imple.RuleServiceImpl;
import com.huatu.tiku.schedule.biz.util.*;
import com.huatu.tiku.schedule.biz.vo.*;
import com.huatu.tiku.schedule.biz.vo.Schedule.PageVo;
import com.huatu.tiku.schedule.biz.vo.Schedule.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.schedule.base.config.CustomUser;
import com.huatu.tiku.schedule.base.exception.BadRequestException;
import com.huatu.tiku.schedule.biz.enums.ExamType;
import com.huatu.tiku.schedule.biz.enums.TeacherType;
import com.huatu.tiku.schedule.biz.service.CourseLiveService;
import com.huatu.tiku.schedule.biz.service.CourseService;
import com.huatu.tiku.schedule.biz.vo.CourseLivePackage.CourseLiveResultVo;
import com.huatu.tiku.schedule.biz.vo.CourseLivePackage.CourseLiveVo;
import com.huatu.tiku.schedule.biz.vo.CourseLivePackage.DateFormatVo;
import com.huatu.tiku.schedule.biz.vo.CourseLivePackage.DayOfWeekVo;
import com.huatu.tiku.schedule.biz.vo.CourseLivePackage.TimeRangeVo;

/**
 * 课程直播Controller
 *
 * @author Geek-S
 *
 */
@RestController
@RequestMapping("courseLive")
public class CourseLiveController {

	private final CourseService courseService;

	private final CourseLiveService courseLiveService;

	private final CourseLiveTeacherService courseLiveTeacherService;

	private final RuleServiceImpl ruleService;

	private final TeacherService teacherService;

	@Autowired
	public CourseLiveController(CourseService courseService, CourseLiveService courseLiveService,
                                CourseLiveTeacherService courseLiveTeacherServiceService,
                                RuleServiceImpl ruleService, TeacherService teacherService) {
		this.courseService = courseService;
		this.courseLiveService = courseLiveService;
		this.courseLiveTeacherService=courseLiveTeacherServiceService;
		this.ruleService = ruleService;
        this.teacherService = teacherService;
    }

	/**
	 * 获取课程直播
	 *
	 * @param courseId
	 *            课程ID
	 * @return 课程直播列表
	 */
	@GetMapping("getCourseLive")
	public CourseLiveResultVo getCourseLive(Long courseId){
        if (courseId == null) {
            throw new BadRequestException("课程ID不能为空");
        }
        Course course = courseService.findOne(courseId);//数据库查询课程数据
        if (course == null) {
            throw new BadRequestException("课程不存在，课程ID [" + courseId + "]");
        }
        List<CourseLive> courseLives = course.getCourseLives();//取出直播集合
        //对直播日期排序
        Collections.sort(courseLives, (o1, o2) -> o1.getDateInt()-o2.getDateInt());
		CourseLiveResultVo courseLiveVoBean=new CourseLiveResultVo(course);//创建返回结果
		if(courseLives!=null&&!courseLives.isEmpty()){
			Set set=new TreeSet();//日期集合 临时存储使用
			List<List> days=new ArrayList<>();//直播数据按日期分类的集合
			Set<TimeRangeVo> timeRangeVoSet =new TreeSet(new TimeRangeVo.MyComparator());//时间段集合 按开始结束时间排序
			for(CourseLive courseLive:courseLives) {//先循环一遍 取出所有直播时间段
				Integer timeBegin = courseLive.getTimeBegin();
				Integer timeEnd = courseLive.getTimeEnd();
				TimeRangeVo timeRangeVo =new TimeRangeVo();
				timeRangeVo.setTimeBegin(timeBegin);
				timeRangeVo.setTimeEnd(timeEnd);
				timeRangeVoSet.add(timeRangeVo);//存入时间范围集合
			}
			int timeRangeSize = timeRangeVoSet.size();//时间段个数就是每天直播总条数 没有直播的创建空直播数据
			for(CourseLive courseLive:courseLives) {
				CourseLiveVo courseLiveVo = new CourseLiveVo(courseLive);//将直播转成直播vo
				Date date = courseLive.getDate();//取出直播日期
				Integer timeBegin = courseLive.getTimeBegin();//取出直播开始时间
				Integer timeEnd = courseLive.getTimeEnd();//取出直播结束时间
				int index = TimeRangeUtil.getIndex(timeRangeVoSet, timeBegin, timeEnd);//当前直播在当天直播时间段序号
				DateFormatVo dateFormat=new DateFormatVo(date);
				if(set.add(date)){//日期添加成功 没有这个日期
					List<Object> dayData=new ArrayList();//创建每天的数据集合
					dayData.add(false);//第一行空数据
					dayData.add(dateFormat);//第二行日期
					DayOfWeekVo dayOfWeek=new DayOfWeekVo(date);
					dayData.add(dayOfWeek);//第三行添加星期
					for(int i=1;i<=timeRangeSize;i++){//循环直播次数
						if(i==index){//到当前直播位置时添加进集合
							dayData.add(courseLiveVo);//第四行添加直播集合
						}else{//不是时添加空数据
							dayData.add(new CourseLiveVo());//空直播数据插入占位
						}
					}
					days.add(dayData);
				}else{//日期添加失败 有这个日期  添加到这个日期的集合
					for(List day:days){//查找每个日期数据
						DateFormatVo dateBean=(DateFormatVo)day.get(1);//第二条数据是日期
						if(dateFormat.getDateValue().equals(dateBean.getDateValue())){//如果是同一天
							day.remove(2+index);//下标3 第四个元素为第一个 删除原本占位null
							day.add(2+index, courseLiveVo);//直播添加进去
						}
					}
				}
			}
			courseLiveVoBean.setBody(days);//每日数据存入
			List<String> head=new ArrayList();
			for(TimeRangeVo timeRangeVo : timeRangeVoSet){//拼接时间范围字符串
				StringBuilder sb=new StringBuilder();
				String timeBegin = intToDateString(timeRangeVo.getTimeBegin());
				String timeEnd = intToDateString(timeRangeVo.getTimeEnd());
				head.add(sb.append("授课时间").append(timeBegin).append("-").append(timeEnd).toString());
			}
		courseLiveVoBean.setHead(head);//播放时间表头存入
		}
		return courseLiveVoBean;
	}

	/**
	 * 新增课程直播
	 *
	 * @param courseLiveDto
	 *            课程直播
	 * @return 课程直播
	 */
	@PostMapping("createCourseLive")
	public List<CourseLive> createCourseLive(@Valid @RequestBody CreateCourseLiveDto courseLiveDto,
			BindingResult bindingResult) {
		// 校验参数是否合法
		if (bindingResult.hasErrors()) {
			throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
		}

		return courseLiveService.createCourseLive(courseLiveDto.getCourseId(),null,courseLiveDto.getDates(), courseLiveDto.getTimes(),courseLiveDto.getToken());
	}
	/**添加课程状态判断 直播安排状态抛出异常
	 * 新增课程直播
	 *
	 * @param courseLiveDto
	 *            课程直播
	 * @return 课程直播
	 */
	@PostMapping("createCourseLiveByStatus")
	public List<CourseLive> createCourseLiveByStatus(@Valid @RequestBody CreateCourseLiveDto courseLiveDto,
			BindingResult bindingResult) {
		// 校验参数是否合法
		if (bindingResult.hasErrors()) {
			throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
		}
		courseService.findCourseStatusByCourseId(courseLiveDto.getCourseId());//如果为直播安排状态会抛出异常
		return courseLiveService.createCourseLive(courseLiveDto.getCourseId(),null, courseLiveDto.getDates(), courseLiveDto.getTimes(),courseLiveDto.getToken());
	}

	/**
	 * 删除课程直播
	 * 
	 * @param idsDto
	 *            课程直播ID
	 * @return 操作结果
	 */
	@PostMapping("deleteCourseLive")
	public Boolean deleteCourseLive(@Valid @RequestBody IdsDto idsDto, BindingResult bindingResult) {
		// 校验参数是否合法
		if (bindingResult.hasErrors()) {
			throw new BadRequestException("课程直播" + bindingResult.getAllErrors().get(0).getDefaultMessage());
		}

		courseLiveService.delete(idsDto.getIds());

		return true;
	}

	/**
	 * 根据课程ID和日期删除课程直播
	 *
	 * @param deleteCourseLiveBatchDto
	 *            课程直播ID/日期
	 * @return 操作结果
	 */
	@PostMapping("deleteCourseLiveBatch")
	public Boolean deleteCourseLiveBatch(@Valid @RequestBody DeleteCourseLiveBatchDto deleteCourseLiveBatchDto,
			BindingResult bindingResult) {
		// 校验参数是否合法
		if (bindingResult.hasErrors()) {
			throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
		}

		List<CourseLive> courseLives = Lists.newArrayList();

		if (deleteCourseLiveBatchDto.getTimes() == null) {
			List<CourseLive> courseLivesTemp = courseLiveService.findByCourseIdAndDateIn(
					deleteCourseLiveBatchDto.getCourseId(), deleteCourseLiveBatchDto.getDates());

			courseLives.addAll(courseLivesTemp);
		} else {
			deleteCourseLiveBatchDto.getTimes().forEach(time -> {
				Integer timeBegin = Integer.parseInt(time.get(0).replace(":", ""));
				Integer timeEnd = Integer.parseInt(time.get(1).replace(":", ""));

				List<CourseLive> courseLivesTemp = courseLiveService.findByCourseIdAndDateInAndTimeBeginAndTimeEnd(
						deleteCourseLiveBatchDto.getCourseId(), deleteCourseLiveBatchDto.getDates(), timeBegin,
						timeEnd);

				courseLives.addAll(courseLivesTemp);
			});

		}

		courseLiveService.delete(courseLives);

		return true;
	}

	/**添加课程状态判断 直播安排状态抛出异常
	 * 根据课程ID和日期删除课程直播
	 *
	 * @param deleteCourseLiveBatchDto
	 *            课程直播ID/日期
	 * @return 操作结果
	 */
	@PostMapping("deleteCourseLiveBatchByStatus")
	public Boolean deleteCourseLiveBatchByStatus(@Valid @RequestBody DeleteCourseLiveBatchDto deleteCourseLiveBatchDto,
			BindingResult bindingResult) {
		// 校验参数是否合法
		if (bindingResult.hasErrors()) {
			throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
		}
		courseService.findCourseStatusByCourseId(deleteCourseLiveBatchDto.getCourseId());//如果为直播安排状态会抛出异常

		List<CourseLive> courseLives = courseLiveService.findByCourseIdAndDateIn(deleteCourseLiveBatchDto.getCourseId(),
				deleteCourseLiveBatchDto.getDates());

		courseLiveService.delete(courseLives);

		return true;
	}

	/**
	 * 获取教师课表
	 * @param dateBegin
	 *            日期开始
	 * @param dateEnd
	 *            日期结束
	 * @param teacherId
	 * 				教师id
	 * @return 课程直播
	 */
	@GetMapping("schedule")
	public PageVo schedule(@DateTimeFormat(pattern = "yyyy-MM-dd") Date dateBegin,
			@DateTimeFormat(pattern = "yyyy-MM-dd") Date dateEnd, Long teacherId,
                           @AuthenticationPrincipal CustomUser user,Pageable page) {
		if (dateBegin == null || dateEnd == null) {
			throw new BadRequestException("日期不能为空");
		}

		if (dateBegin.after(dateEnd)) {
			throw new BadRequestException("开始时间不能晚于结束时间");
		}
        ExamType teacherExamType=null;
        Long teacherSubjectId=null;
        Long subjectId=null;
        // 考试类型
		List<ExamType> examTypes = Lists.newArrayList();
		// 可以查看全部的角色
		List<String> roleNames = Lists.newArrayList("超级管理员", "人力");
		// 当前用户角色
		Set<Role> roles = user.getRoles();
		// 管理员
		Optional<Role> adminFlag = roles.stream().filter(role -> roleNames.contains(role.getName())).findFirst();
		if (!adminFlag.isPresent()) { //不是指定角色
            if(teacherId!=null) {
                Teacher teacher = teacherService.findOne(teacherId);
                teacherExamType = teacher.getExamType();//查询教师的考试类型
                teacherSubjectId = teacher.getSubjectId();//查询教师的考试科目
            }
			Optional<Role> jwFlag = roles.stream().filter(role -> role.getName().equals("教务")).findFirst();
			if (jwFlag.isPresent()) { // 教务
				// 数据权限
				Set<ExamType> dataPermissioins = user.getDataPermissions();
				if (teacherExamType != null) { //勾选教师情况
					// 判断是否拥有该教师所在组权限
					if (!dataPermissioins.contains(teacherExamType)) {
						throw new BadRequestException("无【" + teacherExamType.getText() + "】的查看权限");
					}
				}else{//将自己拥有权限填入
                    examTypes.addAll(dataPermissioins);
                }
			} else { //不是教务判断是否是组长
				Boolean leaderFlag1 = user.getLeaderFlag();
				if (leaderFlag1) { //组长
                    if (teacherExamType != null) { //勾选教师情况
                        // 判断是否该教师所在组组长
                        if (!teacherExamType.equals(user.getExamType())) {
                            throw new BadRequestException("无【" + teacherExamType.getText() + "】的教师查看权限");
                        }
                    }else{ //将自己拥有权限填入
                        examTypes.add(user.getExamType());
                    }
					if(teacherSubjectId!=null){//勾选教师 查询组长所在科目
                        if(!teacherSubjectId.equals(user.getSubjectId())){
                            throw new BadRequestException("非该科目组长,无权限查看");
                        }
                    }else {
                        subjectId=user.getSubjectId();
					}
				} else { //不是组长报错
					throw new BadRequestException("无讲师课表的查看权限");
				}
			}
		}

		// 组装表头
		Calendar start = Calendar.getInstance();
		start.setTime(dateBegin);

		List<String> headers = Lists.newArrayList();

		for (; start.getTime().before(dateEnd); start.roll(Calendar.DAY_OF_YEAR, true)) {
			headers.add(DateformatUtil.format1(start.getTime() ) );
		}

		headers.add(DateformatUtil.format1(start.getTime() ) );

		Page<CourseLiveScheduleVo> courseLiveScheduleVos = courseLiveService.schedule(examTypes, subjectId, dateBegin,
				dateEnd, teacherId,page);
		List<Object> result=new ArrayList<>();
		String dateString=null;//临时字符串 记录日期
		for(CourseLiveScheduleVo vo:courseLiveScheduleVos.getContent()){
		    if(!vo.getDate().equals(dateString)){//不相等添加日期
                dateString=vo.getDate();
                result.add(ImmutableMap.of("date", dateString));
            }
            Schedule schedule=new Schedule();
            schedule.setTime(vo.getTimeBegin()+"-"+vo.getTimeEnd());//时间
            schedule.setAssistantName(vo.getAssistantName());//助教
            schedule.setCompereName(vo.getCompereName());//主持人
            schedule.setCtrlName(vo.getControllerName());//场控
            schedule.setLtName(vo.getLearningTeacherName());//学习师名字
            schedule.setCourseName(vo.getCourseName());//课程名
            schedule.setLiveName(vo.getCourseLiveName());//直播名
			schedule.setCategoryName(vo.getCategoryName());//课程类型
			schedule.setExamtypeSubject(vo.getSubject());
            List<CourseLiveScheduleVo.TeacherInfo> teacherInfos = vo.getTeacherInfos();
            if(teacherInfos!=null&&!teacherInfos.isEmpty()){
                StringBuffer sb=new StringBuffer();
                teacherInfos.forEach(info->{
                    sb.append(info.getName());
                        sb.append(",");
                 });
                int length = sb.length();
                sb.deleteCharAt(length-1);
                schedule.setTeacherNames(sb.toString());//教师名
            }
            result.add(schedule);
        }
		PageVo pageVo=new PageVo();
		pageVo.setContent(result);
		pageVo.setFirst(courseLiveScheduleVos.isFirst());
		pageVo.setLast(courseLiveScheduleVos.isLast());
		pageVo.setNumber(courseLiveScheduleVos.getNumber());
		pageVo.setNumberOfElements(courseLiveScheduleVos.getNumberOfElements());
		pageVo.setSize(courseLiveScheduleVos.getSize());
		pageVo.setTotalElements(courseLiveScheduleVos.getTotalElements());
		pageVo.setTotalPages(courseLiveScheduleVos.getTotalPages());
		return pageVo;
	}



    /**
     * 修改直播绑定直播间 添加课程状态判断 直播安排状态抛出异常
     * @param updateLiveRoomDto  修改直播绑定直播间
     * @param bindingResult 验证
     * @return 成功true
     */
    @PostMapping("updateLiveRoom")
    public Boolean updateLiveRoom(@Valid @RequestBody UpdateLiveRoomDto updateLiveRoomDto,
                                  BindingResult bindingResult){
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
		courseLiveService.findCourseStatusByCourseLiveId(updateLiveRoomDto.getLiveId());//如果为直播安排状态会抛出异常
        return 0 != courseLiveService.updateLiveRoom(updateLiveRoomDto.getLiveId(), updateLiveRoomDto.getRoomId());

    }

	/**
	 * 滚动排课查询
	 *
	 * @param currentCourseId
	 *            当前课程ID
	 * @param dates
	 *            日期
	 * @param courseId
	 *            课程ID
	 * @param examType
	 *            考试类型
     * @param subjectId
     *            科目ID
	 * @return List
	 */
	@GetMapping("findForRolling")
	public List<CourseLiveRollingVo> findForRolling(Long currentCourseId, @DateTimeFormat(pattern = "yyyy-MM-dd") Date[] dates, Long courseId,
			ExamType examType, Long subjectId) {
		if (currentCourseId == null) {
			throw new BadRequestException("当前课程ID不能为空");
		}

		if (dates == null || dates.length == 0) {
			throw new BadRequestException("日期不能为空");
		}

		return courseLiveService.findForRolling(currentCourseId, Arrays.asList(dates), courseId, examType, subjectId);
	}

	/**
	 * 提交滚动排课
	 * 
	 * @param rollingScheduleDto
	 *            滚动排课参数
	 */
	@PostMapping("rollingSchedule")
	public Boolean rollingSchedule(@Valid @RequestBody RollingScheduleDto rollingScheduleDto,
			BindingResult bindingResult) {
		// 校验参数是否合法
		if (bindingResult.hasErrors()) {
			throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
		}
		courseService.findCourseStatusByCourseId(rollingScheduleDto.getCourseId());//如果为直播安排状态会抛出异常

		courseLiveService.rollingSchedule(rollingScheduleDto.getCourseId(), rollingScheduleDto.getCourseLiveIds());

		return true;
	}

	/**
	 * 修改直播绑定助教(主持人等)
	 * @param updateAssistantDto 修改直播绑定助教
	 * @return boolean
	 */
	@PostMapping("updateAssistant")
	public Boolean updateAssistant(@Valid @RequestBody UpdateBindAssistantDto updateAssistantDto,
			BindingResult bindingResult) {
		// 校验参数是否合法
		if (bindingResult.hasErrors()) {
			throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
		}
		TeacherType teacherType = updateAssistantDto.getTeacherType();
		if(teacherType.equals(TeacherType.JS)){
			throw new BadRequestException("错误的教师类型");
		}
		Long liveId = updateAssistantDto.getLiveId();
		Long assistantId = updateAssistantDto.getTeacherId();
		return 0!=courseLiveService.updateAssistant(liveId,assistantId,teacherType);
	}


	/**
	 * 修改直播名字 添加课程状态判断 直播安排状态抛出异常
	 * @param updateLiveNameDto 直播id 直播名
	 * @return result
	 */
	@PostMapping("updateLiveName")
	public Boolean updateLiveName(@Valid @RequestBody UpdateLiveNameDto updateLiveNameDto,
			BindingResult bindingResult) {
		// 校验参数是否合法
		if (bindingResult.hasErrors()) {
			throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
		}
		courseLiveService.findCourseStatusByCourseLiveId(updateLiveNameDto.getLiveId());//如果为直播安排状态会抛出异常
		return 0!=courseLiveService.updateLiveName(updateLiveNameDto.getLiveId(),updateLiveNameDto.getLiveName());
	}

	/**
	 * 一键排课
	 * 
	 * @param oneKeyScheduleDto
	 *            课程ID 日期
	 * @return 结果
	 */
	@PostMapping("oneKeySchedule")
	public Boolean oneKeySchedule(@Valid @RequestBody OneKeyScheduleDto oneKeyScheduleDto, BindingResult bindingResult) {
		// 校验参数是否合法
		if (bindingResult.hasErrors()) {
			throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
		}
		courseService.findCourseStatusByCourseId(oneKeyScheduleDto.getCourseId());//如果为直播安排状态会抛出异常

		courseLiveService.oneKeySchedule(oneKeyScheduleDto.getCourseId(), oneKeyScheduleDto.getDates());

		return true;
	}

	/**
	 * 获取助教课表
	 * 
	 * @param examType
	 *            考试类型
	 * @param subjectId
	 *            科目ID
	 * @param dateBegin
	 *            日期开始
	 * @param dateEnd
	 *            日期结束
	 * @param teacherName
	 *            教师名称
	 * @param courseName
	 *            课程名称
	 * @param liveRoomId
	 *            直播间ID
	 * @return 课程直播
	 */
	@GetMapping("scheduleAssistant")
	public PageVo scheduleAssistant(String courseName, ExamType examType, Long subjectId, Long liveRoomId,
			@DateTimeFormat(pattern = "yyyy-MM-dd") Date dateBegin,
			@DateTimeFormat(pattern = "yyyy-MM-dd") Date dateEnd, String teacherName,
									Pageable page,TeacherType teacherType) {
		if (dateBegin == null || dateEnd == null) {
			throw new BadRequestException("日期不能为空");
		}

		if (dateBegin.after(dateEnd)) {
			throw new BadRequestException("开始时间不能晚于结束时间");
		}

		// 组装表头
		Calendar start = Calendar.getInstance();
		start.setTime(dateBegin);

		List<String> headers = Lists.newArrayList();

		for (; start.getTime().before(dateEnd); start.roll(Calendar.DAY_OF_YEAR, true)) {
			headers.add(DateformatUtil.format2(start.getTime()) + " " + DateformatUtil.format4(start.getTime()));
		}

		headers.add(DateformatUtil.format2(start.getTime()) + " " + DateformatUtil.format4(start.getTime()));

		// 助教
		Page<CourseLiveScheduleAssistantVo> courseLiveScheduleVos = courseLiveService.scheduleAssistant(courseName, examType,
				subjectId, liveRoomId, dateBegin, dateEnd, teacherType, teacherName, page);

        List<Object> result=new ArrayList<>();
        String dateString=null;//临时字符串 记录日期
        for(CourseLiveScheduleAssistantVo vo:courseLiveScheduleVos.getContent()){
            if(!vo.getDate().equals(dateString)){//不相等添加日期
                dateString=vo.getDate();
                result.add(ImmutableMap.of("date", dateString));
            }
            Schedule schedule=new Schedule();
            schedule.setTime(vo.getTimeBegin()+"-"+vo.getTimeEnd());//时间
            schedule.setAssistantName(vo.getAssistantName());//助教
            schedule.setCompereName(vo.getCompereName());//主持人
            schedule.setCtrlName(vo.getControllerName());//场控
            schedule.setLtName(vo.getLearningTeacherName());//学习师名字
            schedule.setCourseName(vo.getCourseName());//课程名
            schedule.setLiveName(vo.getCourseLiveName());//直播名
            schedule.setCategoryName(vo.getCategoryName());
            schedule.setExamtypeSubject(vo.getSubject());
            schedule.setTeacherNames(vo.getTeacherNames());
            result.add(schedule);
        }
        PageVo pageVo=new PageVo();
        pageVo.setContent(result);
        pageVo.setFirst(courseLiveScheduleVos.isFirst());
        pageVo.setLast(courseLiveScheduleVos.isLast());
        pageVo.setNumber(courseLiveScheduleVos.getNumber());
        pageVo.setNumberOfElements(courseLiveScheduleVos.getNumberOfElements());
        pageVo.setSize(courseLiveScheduleVos.getSize());
        pageVo.setTotalElements(courseLiveScheduleVos.getTotalElements());
        pageVo.setTotalPages(courseLiveScheduleVos.getTotalPages());
        return pageVo;
	}

	/**
	 * 获取我的课表助教
	 * 
	 * @param examType
	 *            考试类型
	 * @param subjectId
	 *            科目ID
	 * @param dateBegin
	 *            日期开始
	 * @param dateEnd
	 *            日期结束
	 * @param courseName
	 *            课程名称
	 * @param liveRoomId
	 *            直播间ID
	 * @return 课程直播
	 */
	@GetMapping("myScheduleAssistant")
	public Map<String, Object> scheduleAssistant(String courseName, ExamType examType, Long subjectId, Long liveRoomId,
			@DateTimeFormat(pattern = "yyyy-MM-dd") Date dateBegin,
			@DateTimeFormat(pattern = "yyyy-MM-dd") Date dateEnd, @AuthenticationPrincipal CustomUser user) {
		if (dateBegin == null || dateEnd == null) {
			throw new BadRequestException("日期不能为空");
		}

		if (dateBegin.after(dateEnd)) {
			throw new BadRequestException("开始时间不能晚于结束时间");
		}

		// 组装表头
		Calendar start = Calendar.getInstance();
		start.setTime(dateBegin);

		List<String> headers = Lists.newArrayList();

		for (; start.getTime().before(dateEnd); start.roll(Calendar.DAY_OF_YEAR, true)) {
			headers.add(DateformatUtil.format0(start.getTime()) );
		}

		headers.add(DateformatUtil.format0(start.getTime()) );

		// 直播
		List<CourseLiveScheduleAssistantVo> courseLiveScheduleAssistantVos = courseLiveService.myScheduleAssistant(
				courseName, examType, subjectId, liveRoomId, dateBegin, dateEnd, user.getTeacherType(), user.getId());

		List<List<CourseLiveScheduleAssistantVo>> datas = Lists.newArrayList();

		// 初始化数据容器
		IntStream.range(0, headers.size()).forEach(i -> {
			datas.add(null);
		});

		// 表头放入Map
		Map<String, Integer> dateDic = Maps.newHashMap();

		for (int i = 0; i < headers.size(); i++) {
			dateDic.put(headers.get(i), i);
		}

		// 将数据放入对应日期
		courseLiveScheduleAssistantVos.forEach(courseLiveScheduleAssistantVo -> {
			Integer index = dateDic.get(courseLiveScheduleAssistantVo.getDate());

			List<CourseLiveScheduleAssistantVo> items = datas.get(index);

			if (items == null) {
				items = Lists.newArrayList();
				datas.set(index, items);
			}

			items.add(courseLiveScheduleAssistantVo);
		});

		return ImmutableMap.of( "datas", datas);
	}


	/**
	 * 获取我的课表
	 * 
	 * @param examType
	 *            考试类型
	 * @param subjectId
	 *            科目ID
	 * @param dateBegin
	 *            日期开始
	 * @param dateEnd
	 *            日期结束
	 * @param courseName
	 *            课程名称
	 * @param liveRoomId
	 *            直播间ID
	 * @return 课程直播
	 */
	@GetMapping("mySchedule")
	public Map<String, Object> mySchedule(ExamType examType, Long subjectId,
			@DateTimeFormat(pattern = "yyyy-MM-dd") Date dateBegin,
			@DateTimeFormat(pattern = "yyyy-MM-dd") Date dateEnd, String courseName, Long liveRoomId,
			@AuthenticationPrincipal CustomUser user) {
		if (dateBegin == null || dateEnd == null) {
			throw new BadRequestException("日期不能为空");
		}

		if (dateBegin.after(dateEnd)) {
			throw new BadRequestException("开始时间不能晚于结束时间");
		}

		// 组装表头
		Calendar start = Calendar.getInstance();
		start.setTime(dateBegin);

		List<String> headers = Lists.newArrayList();

		for (; start.getTime().before(dateEnd); start.roll(Calendar.DAY_OF_YEAR, true)) {
			headers.add(DateformatUtil.format0( start.getTime() ) );
		}

		headers.add(DateformatUtil.format0(start.getTime() ) );

		List<CourseLiveScheduleVo> courseLiveScheduleVos = courseLiveService.mySchedule(examType, subjectId, dateBegin,
				dateEnd, courseName, liveRoomId, user.getId());

		List<List<CourseLiveScheduleVo>> datas = Lists.newArrayList();

		IntStream.range(0, headers.size()).forEach(i -> {
			datas.add(null);
		});

		// 表头放入Map
		Map<String, Integer> dateDic = Maps.newHashMap();

		for (int i = 0; i < headers.size(); i++) {
			dateDic.put(headers.get(i), i);
		}

		// 将数据放入对应日期
		courseLiveScheduleVos.forEach(courseLiveScheduleVo -> {
			Integer index = dateDic.get(courseLiveScheduleVo.getDate());

			List<CourseLiveScheduleVo> items = datas.get(index);

			if (items == null) {
				items = Lists.newArrayList();
				datas.set(index, items);
			}

			items.add(courseLiveScheduleVo);
		});

		return ImmutableMap.of("datas", datas);
	}

	private final static Map statisticsMap=new HashMap();

    /**
     * 教师课程统计
     * @param dateBegin 开始时间
     * @param dateEnd 结束时间
     * @param teacherId 教师id
     * @return 统计结果
     */
    @GetMapping("statistics")
	public PageVo statistics(@DateTimeFormat(pattern = "yyyy-MM-dd") Date dateBegin,
                             @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateEnd,
                             Long teacherId,Pageable page,@AuthenticationPrincipal CustomUser user){
        if(dateBegin==null){
            throw new BadRequestException("请设置时间范围");
        }
        if(dateEnd==null){
            dateEnd=new Date();
        }
		Teacher teacher = teacherService.findOne(teacherId);
		ExamType examType = teacher.getExamType();
		// 可以查看全部的角色
		List<String> roleNames = Lists.newArrayList("超级管理员");
		// 当前用户角色
		Set<Role> roles = user.getRoles();
		// 管理员
		Optional<Role> adminFlag = roles.stream().filter(role -> roleNames.contains(role.getName())).findFirst();
		if (!adminFlag.isPresent()) {//如果不是管理员
			Optional<Role> jwFlag = roles.stream().filter(role -> role.getName().equals("教务")).findFirst();
			if (jwFlag.isPresent()) {// 教务
				// 数据权限
				Set<ExamType> dataPermissioins = user.getDataPermissions();
				if (!dataPermissioins.contains(examType)) { // 判断教师是否在权限内
					throw new BadRequestException("无【" + examType.getText() + "】的教师查看权限");
				}
			}else{
				throw new BadRequestException("无【" + examType.getText() + "】的查看权限");
			}
		}

		List<CourseLive> lives = courseLiveService.findByDateAndTeacherId(dateBegin, dateEnd, teacherId,page);
        PageVo pageVo=new PageVo();//返回值
        int start = page.getOffset();
        int end=page.getPageSize()+start;
        List<Object> body=new ArrayList<>();
        if(lives!=null&&!lives.isEmpty()){
            List<CourseLive> arrList = new ArrayList(lives);//arraylist接收
            Collections.reverse(arrList);//逆序 方便处理数据
            String flag=arrList.get(0).getDate().toString();//最后一天日期
            BigDecimal count=BigDecimal.valueOf(0);
            BigDecimal countDay=BigDecimal.valueOf(0);
			DecimalFormat df =new DecimalFormat("#.00");
			for(CourseLive live:arrList){
                if(live.getSourceId()==null){//非滚动直播
                    if(!live.getDate().toString().equals(flag)){
                        body.add(ImmutableMap.of("countDay",flag+" "+df.format(countDay)+"小时"));
						count=count.add(countDay);
						countDay=BigDecimal.valueOf(0);
                        flag=live.getDate().toString();
                    }
                    StatisticsVo statisticsVo=new StatisticsVo();
                    statisticsVo.setDate(live.getDate().toString());
                    Integer timeBegin = live.getTimeBegin();//开始时间
                    Integer timeEnd = live.getTimeEnd();//结束时间
					BigDecimal bigDecimal=TimeUtil.minut2Hour(TimeUtil.interval(timeBegin, timeEnd));//时间差转成小时
					statisticsVo.setCount(df.format(bigDecimal));//小时数
					Float coefficient=1f;//默认系数为1.0

					List<Rule> ruleByData = ruleService.findRuleByData(live.getDateInt(), live.getCourse().getCourseCategory(), live.getCourse().getExamType(),
							live.getCourseLiveCategory());
					if(ruleByData!=null&&!ruleByData.isEmpty()){//有相应的规则
						coefficient=ruleByData.get(0).getCoefficient();//取出相应的规则系数
					}
					bigDecimal=bigDecimal.multiply(new BigDecimal(coefficient));
					countDay=countDay.add(bigDecimal);

                    statisticsVo.setTime(TimeformatUtil.format(timeBegin)+":"+TimeformatUtil.format(timeEnd));
                    statisticsVo.setLiveName(live.getName());
                    statisticsVo.setCoefficient(coefficient);//比例系数
                    statisticsVo.setCategoryName(live.getCourse().getCourseCategory().getText());
                    statisticsVo.setCourseName(live.getCourse().getName());
                    body.add(statisticsVo);
                }
            };
            body.add(ImmutableMap.of("countDay",flag+" "+df.format(countDay)+"小时"));
			count=count.add(countDay);
            Collections.reverse(body);//正序
			statisticsMap.put("body",body);
			statisticsMap.put("dateBegin",dateBegin);
			statisticsMap.put("dateEnd",dateEnd);
			statisticsMap.put("teacherId",teacherId);
			statisticsMap.put("count",df.format(count));
            if(end>body.size()){
                end=body.size();
            }
            pageVo.setContent(ImmutableMap.of("count",df.format(count),"body", body.subList(start,end)));
        }else{
            pageVo.setContent(null);
        }
        int contentSize = body.size();
        int size=contentSize%page.getPageSize()==0?contentSize/page.getPageSize():contentSize/page.getPageSize()+1;
        pageVo.setFirst(page.getPageNumber()==0?true:false);
		pageVo.setLast(page.getPageNumber()==size-1?true:false);
		pageVo.setNumber(page.getPageNumber());
		pageVo.setNumberOfElements(end-start-1);
		pageVo.setSize(page.getPageSize());
		pageVo.setTotalElements(Long.valueOf(body.size()));
		pageVo.setTotalPages(size);
        return pageVo;
    }

	@GetMapping("statisticsPage")
	public PageVo statisticsPage(@DateTimeFormat(pattern = "yyyy-MM-dd") Date dateBegin,
							 @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateEnd,
							 Long teacherId,Pageable page){
		if(dateBegin.equals(statisticsMap.get("dateBegin"))&&dateEnd.equals(statisticsMap.get("dateEnd"))&&teacherId.equals(statisticsMap.get("teacherId"))) {
			List statisticsBody=(List)statisticsMap.get("body");
			String count=(String)statisticsMap.get("count");
			int pageSize = page.getPageSize();
			int pageNumber = page.getPageNumber();
			int start = page.getOffset();//起始条数
			int end = pageSize + start;//结束条数
			if (end > statisticsBody.size()) {
				end = statisticsBody.size();
			}
			PageVo pageVo = new PageVo();//返回值
			pageVo.setContent(ImmutableMap.of("count", count, "body", statisticsBody.subList(start, end)));
			Integer contentSize = statisticsBody.size();//总条数
			int size = contentSize % pageSize == 0 ? contentSize / pageSize : contentSize / pageSize + 1;//总页数
			pageVo.setFirst(pageNumber == 0 ? true : false);//第一页
			pageVo.setLast(pageNumber == size - 1 ? true : false);//最后一页
			pageVo.setNumber(pageNumber);//当前页数
			pageVo.setNumberOfElements(end - start - 1);//当前页条数
			pageVo.setSize(pageSize);//条数
			pageVo.setTotalElements(Long.valueOf(contentSize));//总条数
			pageVo.setTotalPages(size);//总页数
			return pageVo;
		}else{
			return new PageVo();
		}
	}

    /**
     * 修改面试授课类型 添加课程状态判断 直播安排状态抛出异常
     * @param updateCourseLiveCategoryDto 参数
     * @return 结果
     */
    @PostMapping("updateCourseLiveCategoryByStatus")
    public Boolean updateCourseLiveCategoryByStatus(@Valid @RequestBody UpdateCourseLiveCategoryDto updateCourseLiveCategoryDto,
                                                    BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        courseLiveService.findCourseStatusByCourseLiveId(updateCourseLiveCategoryDto.getLiveId());//如果为直播安排状态会抛出异常
        return 0!=courseLiveService.updateCourseLiveCategoryByStatus(updateCourseLiveCategoryDto.getLiveId(),updateCourseLiveCategoryDto.getCourseLiveCategory());
    }


    @GetMapping("getLiveInfo")
    public List<LiveInfoVo> getLiveInfo(Long liveId,Long liveTeacherId,TeacherType type){
        if (liveId==null) {
            throw new BadRequestException("liveId不能为空");
        }
        CourseLiveTeacher liveTeacher =null;
        CourseLive live;
        if(liveTeacherId!=null){
            liveTeacher = courseLiveTeacherService.findOne(liveTeacherId);
            live = liveTeacher.getCourseLive();
        }else{
            live = courseLiveService.findOne(liveId);
        }
        Course course = live.getCourse();
        LiveInfoVo vo=new LiveInfoVo();

        vo.setExamType(course.getExamType());
        vo.setDate(live.getDate().toString()+" "+TimeRangeUtil.intToDateString(live.getTimeBegin())+"-"+TimeRangeUtil.intToDateString(live.getTimeEnd()));
        vo.setCourseId(course.getId());
        vo.setCourseName(course.getName());
        vo.setLiveName(live.getName());
        vo.setLiveId(live.getId());
        switch (type){
			case ZJ:
				vo.setAssflag(course.getAssistantFlag());
				vo.setAssConfirm(live.getAssConfirm());
				Teacher assistant = live.getAssistant();
				if(null!=assistant){
					vo.setAssName(assistant.getName());
					vo.setAssId(assistant.getId());
				}
				break;
			case ZCR:
				vo.setComflag(course.getCompereFlag());
				vo.setComConfirm(live.getComConfirm());
				Teacher compere = live.getCompere();
				if(null!=compere){
					vo.setComName(compere.getName());
					vo.setComId(compere.getId());
				}
				break;
			case CK:
				vo.setCtrlflag(course.getControllerFlag());
				vo.setCtrlConfirm(live.getCtrlConfirm());
				Teacher controller = live.getController();
				if(null!=controller){
					vo.setCtrlName(controller.getName());
					vo.setCtrlId(controller.getId());
				}
				break;
			case XXS:
				vo.setLtflag(course.getLearningTeacherFlag());
				vo.setLtConfirm(live.getLtConfirm());
				Teacher learningTeacher = live.getLearningTeacher();
				if(null!=learningTeacher){
					vo.setLtName(learningTeacher.getName());
					vo.setLtId(learningTeacher.getId());
				}
				break;

		}
        List<LiveInfoVo.LiveTeacher> list=new ArrayList();

        if(TeacherType.JS.equals(type)){//讲师类型
            LiveInfoVo.LiveTeacher lt=new LiveInfoVo.LiveTeacher();
            lt.setId(liveTeacher.getId());
            lt.setCoursePhase(liveTeacher.getCoursePhase().getText());
            lt.setSubjectId(liveTeacher.getSubjectId());
            Subject subject = liveTeacher.getSubject();
            if(subject!=null){
                lt.setSubjectName(liveTeacher.getSubject().getName());
            }
            lt.setModuleId(liveTeacher.getModuleId());
            Module module = liveTeacher.getModule();
            if(module!=null){
                lt.setModuleName(liveTeacher.getModule().getName());
            }
            lt.setLever(liveTeacher.getTeacherCourseLevel());
            lt.setTeacherId(liveTeacher.getTeacherId());
            Teacher teacher = liveTeacher.getTeacher();
            if(null!=teacher){
                lt.setTeacherName(teacher.getName());
            }
			lt.setConfirm(liveTeacher.getConfirm());
            lt.setReadOnly(false);
            list.add(lt);
        }else{
            live.getCourseLiveTeachers().forEach(clt->{
                LiveInfoVo.LiveTeacher lt=new LiveInfoVo.LiveTeacher();
                lt.setId(clt.getId());
                lt.setCoursePhase(clt.getCoursePhase().getText());
                lt.setSubjectId(clt.getSubjectId());
                Subject subject = clt.getSubject();
                if(subject!=null){
                    lt.setSubjectName(clt.getSubject().getName());
                }
                lt.setModuleId(clt.getModuleId());
                Module module = clt.getModule();
                if(module!=null){
                    lt.setModuleName(clt.getModule().getName());
                }
                lt.setLever(clt.getTeacherCourseLevel());
                lt.setTeacherId(clt.getTeacherId());
                Teacher teacher = clt.getTeacher();
                if(null!=teacher){
                    lt.setTeacherName(teacher.getName());
                }
				lt.setConfirm(clt.getConfirm());
                lt.setReadOnly(true);
				list.add(lt);
            });
        }
        vo.setLiveTeachers(list);
        return Lists.newArrayList(vo);
    }

    /**
     * 提交待沟通教师安排
     * @param submitDGTDto 参数
     * @param bindingResult 验证
     * @return 结果
     */
    @PostMapping("submitCourseLiveTeacherDGT")
    public Boolean submitCourseLiveTeacherDGT(@Valid @RequestBody SubmitDGTDto submitDGTDto, BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException( bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        courseLiveService.submitCourseLiveTeacherDGT(submitDGTDto.getLiveId(),submitDGTDto.getLiveTeacherId(),submitDGTDto.getTeacherType(),
				submitDGTDto.getTeacherId(),submitDGTDto.getLevel());

        return true;
    }

}
