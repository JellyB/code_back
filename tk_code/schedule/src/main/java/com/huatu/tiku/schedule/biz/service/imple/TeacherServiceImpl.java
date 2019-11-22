package com.huatu.tiku.schedule.biz.service.imple;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huatu.tiku.schedule.base.exception.BadRequestException;
import com.huatu.tiku.schedule.base.service.impl.BaseServiceImpl;
import com.huatu.tiku.schedule.biz.bean.ImportTeacherCourseBean;
import com.huatu.tiku.schedule.biz.bean.TeacherScoreBean;
import com.huatu.tiku.schedule.biz.domain.*;
import com.huatu.tiku.schedule.biz.dto.php.PHPUpdateTeacherDto;
import com.huatu.tiku.schedule.biz.enums.*;
import com.huatu.tiku.schedule.biz.repository.*;
import com.huatu.tiku.schedule.biz.service.TeacherService;
import com.huatu.tiku.schedule.biz.service.intelligence.IntelligenceHandler;
import com.huatu.tiku.schedule.biz.util.DateformatUtil;
import com.huatu.tiku.schedule.biz.vo.OptionVo;
import com.huatu.tiku.schedule.biz.vo.TeacherVo;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.criteria.*;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@EnableAspectJAutoProxy
public class TeacherServiceImpl extends BaseServiceImpl<Teacher, Long> implements TeacherService {


	Logger LOG = LoggerFactory.getLogger(TeacherServiceImpl.class);

	private final TeacherRepository teacherRepository;
	private final TeacherSubjectRepository teacherSubjectRepository;
	private final CourseRepository courseRepository;
	private final CourseLiveRepository courseLiveRepository;
	private final CourseLiveTeacherRepository courseLiveTeacherRepository;
	private final LiveRoomRepository liveRoomRepository;
	@Resource
	private IntelligenceHandler intelligenceHandler;

	@Autowired
	public TeacherServiceImpl(TeacherRepository teacherRepository, TeacherSubjectRepository teacherSubjectRepository,
							  CourseRepository courseRepository,
							  CourseLiveRepository courseLiveRepository, LiveRoomRepository liveRoomRepository,
							  CourseLiveTeacherRepository courseLiveTeacherRepository) {
		this.teacherRepository = teacherRepository;
		this.teacherSubjectRepository = teacherSubjectRepository;
		this.courseRepository = courseRepository;
		this.courseLiveRepository = courseLiveRepository;
		this.liveRoomRepository = liveRoomRepository;
		this.courseLiveTeacherRepository = courseLiveTeacherRepository;
	}

	@Override
    @Transactional
	public Teacher saveX(Teacher teacher,List<TeacherSubject> teacherSubjects){
        boolean exists = teacherRepository.existsByPhone(teacher.getPhone());
        if(exists){
            throw new BadRequestException("该手机号码已注册");
        }
        teacher = teacherRepository.save(teacher);
		Long teacherId = teacher.getId();//主键id
		if(teacherSubjects!=null&&!teacherSubjects.isEmpty()){
			Iterator<TeacherSubject> iterator = teacherSubjects.iterator();
			while(iterator.hasNext()){//添加主键
				TeacherSubject subject=iterator.next();
				if(subject.getExamType()==null||subject.getSubjectId()==null||subject.getTeacherCourseLevel()==null){//有一个为空删除
					iterator.remove();
				}else{//都不为空添加主键
					subject.setTeacherId(teacherId);
				}
			}
			teacherSubjectRepository.save(teacherSubjects);
		}
		if(teacher.getTeacherType().equals(TeacherType.JS)){//讲师类型
			if(teacher.getLeaderFlag()){//组长类型
				teacherRepository.saveRolesById(teacherId, 8l);
			}else{//非组长类型
				teacherRepository.saveRolesById(teacherId, 2l);
			}
		}else{//助教类型
			teacherRepository.saveRolesById(teacherId, 3l);
		}
		return  teacher;
	}

	@Override
	public Page<TeacherVo> getTeacherList(ExamType examType, String name,Long id, Long subjectId,
										Boolean leaderFlag, TeacherStatus status, TeacherType teacherType,Pageable page) {

		Specification<Teacher> querySpecific = new Specification<Teacher>() {
			@Override
			public Predicate toPredicate(Root<Teacher> root, CriteriaQuery<?> criteriaQuery,
										 CriteriaBuilder criteriaBuilder) {

				List<Predicate> predicates = new ArrayList<>();
				if (examType != null) {
					predicates.add(criteriaBuilder.equal(root.get("examType"), examType));
				}if(!Strings.isNullOrEmpty(name)){
					predicates.add(criteriaBuilder.like(root.get("name"),"%"+name+"%"));
				}if (id != null) {
					predicates.add(criteriaBuilder.equal(root.get("id"), id));
				}
				if (subjectId != null) {
					predicates.add(criteriaBuilder.equal(root.get("subjectId"), subjectId));
				}
				if (leaderFlag != null) {
					predicates.add(criteriaBuilder.equal(root.get("leaderFlag"), leaderFlag ));
				}
				if (status != null) {
					predicates.add(criteriaBuilder.equal(root.get("status"), status));
				}
				if (teacherType != null) {
					if(teacherType.equals(TeacherType.JS)){//讲师类型
						predicates.add(criteriaBuilder.equal(root.get("teacherType"), TeacherType.JS));
					}else{
						predicates.add(criteriaBuilder.notEqual(root.get("teacherType"), TeacherType.JS));
					}
				}

				return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		};
		List<Sort.Order> list=new ArrayList();
		list.add(new Sort.Order(Sort.Direction.ASC, "status"));//状态正旭
		list.add(new Sort.Order(Sort.Direction.DESC, "id"));//id倒序
		Pageable pageable=new PageRequest(page.getPageNumber(),page.getPageSize(),new Sort(list));
		Page<Teacher> teachers = teacherRepository.findAll(querySpecific, pageable);
		List<Teacher> content = teachers.getContent();
		List<TeacherVo> teacherVos=new ArrayList();
		if(content!=null&&!content.isEmpty()){
            for(Teacher teacher:content) {
                TeacherVo teacherVo = new TeacherVo(teacher);
                teacherVos.add(teacherVo);
            }
        }
		return new PageImpl(teacherVos,page,teachers == null ? 0 : teachers.getTotalElements());
	}

	@Override
	public int updateTeacherStatus(List<Long> ids, TeacherStatus status,Long id) {
		return teacherRepository.updateTeacherStatus(ids,status,id);
	}

	@Override
	public List<TeacherScoreBean> getAvailableTeachers(Date date, Integer timeBegin, Integer timeEnd, ExamType examType,
			Long subjectId, TeacherCourseLevel teacherCourseLevel,Long courseId,Long moduleId) {
		List<Teacher> teachers=null;
		List<Long> ids=null;
		if(courseId!=null&&examType.equals(ExamType.MS)){//面试类型 先去查找推荐教师
			ids=teacherRepository.findTeacherByCourseId(courseId);
		}
		// 获取所有符合条件的教师
		teachers = teacherRepository.findAll(new Specification<Teacher>() {
			@Override
			public Predicate toPredicate(Root<Teacher> root, CriteriaQuery<?> criteriaQuery,
					CriteriaBuilder criteriaBuilder) {
				criteriaQuery.distinct(true);

				List<Predicate> predicates = new ArrayList<>();

				Join<Teacher, TeacherSubject> teacherSubjects = root.join("teacherSubjects");

				if(moduleId!=null){
					predicates.add(criteriaBuilder.equal(teacherSubjects.get("moduleId"), moduleId));
				} else if(subjectId != null) {
					predicates.add(criteriaBuilder.equal(teacherSubjects.get("subjectId"), subjectId));
				}else if(examType != null){
					predicates.add(criteriaBuilder.equal(teacherSubjects.get("examType"), examType));
				}

				if (teacherCourseLevel != null) {
					predicates
							.add(criteriaBuilder.equal(teacherSubjects.get("teacherCourseLevel"), teacherCourseLevel));
				}

				// 审核状态
				predicates.add(criteriaBuilder.equal(root.get("status"), TeacherStatus.ZC));
				// 教师类型
				predicates.add(criteriaBuilder.equal(root.get("teacherType"), TeacherType.JS));


				return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));

			}
		});


		List<TeacherScoreBean> teacherScoreBeans = Lists.newArrayList();
		teachers.forEach(teacher -> {
			TeacherScoreBean teacherScoreBean = new TeacherScoreBean();
			teacherScoreBean.setId(teacher.getId());
			teacherScoreBean.setName(teacher.getName());
			teacherScoreBean.setScore(0);
			teacherScoreBeans.add(teacherScoreBean);
		});

		Iterator<TeacherScoreBean> teacherScoreBeanIterator = teacherScoreBeans.iterator();
		while (teacherScoreBeanIterator.hasNext()) {
			TeacherScoreBean teacherScoreBean = teacherScoreBeanIterator.next();

			// 获取上课时间和请假时间
			List<Object[]> times = teacherRepository.getUnavailableTime(teacherScoreBean.getId(), date);
			for (Object[] time : times) {
				Integer begin = (Integer) time[1];
				Integer end = (Integer) time[2];

				if ((timeEnd >= begin && timeEnd <= end) || (timeBegin >= begin && timeBegin <= end)
						|| (timeBegin <= begin && timeEnd >= end)) {
					// 时间有冲突
					teacherScoreBeanIterator.remove();
					break;
				}
			}
		}
		//对面试教师排序
		if(ids!=null&&!ids.isEmpty()){
			for(TeacherScoreBean bean:teacherScoreBeans){
				if(ids.contains(BigInteger.valueOf(bean.getId()))){//如果推荐教师集合有此id
					Integer score = bean.getScore();//取出评分
					score+=50;//推荐教师+50分
					bean.setScore(score);
				};
			};
		}
		teacherScoreBeans.sort((o1,o2)-> o2.getScore()-o1.getScore());

		return teacherScoreBeans;
	}

	@Override
	public List<TeacherScoreBean> autoGetAvailableTeachers(Date date, Integer timeBegin, Integer timeEnd, ExamType examType,
			Long subjectId, TeacherCourseLevel teacherCourseLevel,Long courseId,Long moduleId) {
        List<Teacher> teachers=null;
        List<Long> ids=null;
        if(courseId!=null&&examType.equals(ExamType.MS)){//面试类型 先去查找推荐教师
            ids=teacherRepository.findTeacherByCourseId(courseId);
        }
		if(teacherCourseLevel==null){//授课级别空 添加默认等级
			teacherCourseLevel=TeacherCourseLevel.COMMON;
		}
		TeacherCourseLevel finalTeacherCourseLevel = teacherCourseLevel;
		// 获取所有符合条件的教师
		teachers = teacherRepository.findAll(new Specification<Teacher>() {
			@Override
			public Predicate toPredicate(Root<Teacher> root, CriteriaQuery<?> criteriaQuery,
					CriteriaBuilder criteriaBuilder) {
				criteriaQuery.distinct(true);

				List<Predicate> predicates = new ArrayList<>();

				Join<Teacher, TeacherSubject> teacherSubjects = root.join("teacherSubjects");

				if(moduleId!=null){
					predicates.add(criteriaBuilder.equal(teacherSubjects.get("moduleId"), moduleId));
				} else if(subjectId != null) {
					predicates.add(criteriaBuilder.equal(teacherSubjects.get("subjectId"), subjectId));
				}else if(examType != null){
					predicates.add(criteriaBuilder.equal(teacherSubjects.get("examType"), examType));
				}
				if (finalTeacherCourseLevel != null) {
                    predicates
							.add(criteriaBuilder.greaterThanOrEqualTo(teacherSubjects.get("teacherCourseLevel"), finalTeacherCourseLevel));
                }
                // 审核状态
                predicates.add(criteriaBuilder.equal(root.get("status"), TeacherStatus.ZC));
                // 教师类型
                predicates.add(criteriaBuilder.equal(root.get("teacherType"), TeacherType.JS));

                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		});

		List<TeacherScoreBean> teacherScoreBeans = Lists.newArrayList();
		teachers.forEach(teacher -> {
			TeacherScoreBean teacherScoreBean = new TeacherScoreBean();
			teacherScoreBean.setId(teacher.getId());
			teacherScoreBean.setName(teacher.getName());
            teacherScoreBean.setScore(0);
			teacher.getTeacherSubjects().forEach(subject->{
				if(moduleId!=null){
					if(moduleId.equals(subject.getModuleId())){
						if(finalTeacherCourseLevel.equals(subject.getTeacherCourseLevel())){
							teacherScoreBean.setScore(60);
						}
					}
				}else if(subjectId!=null){
					if(subjectId.equals(subject.getSubjectId())){
						if(finalTeacherCourseLevel.equals(subject.getTeacherCourseLevel())) {
							teacherScoreBean.setScore(60);
						}
					}
				}
			});
			teacherScoreBeans.add(teacherScoreBean);
		});

		Iterator<TeacherScoreBean> teacherScoreBeanIterator = teacherScoreBeans.iterator();
		while (teacherScoreBeanIterator.hasNext()) {
			TeacherScoreBean teacherScoreBean = teacherScoreBeanIterator.next();

			// 获取上课时间和请假时间
			List<Object[]> times = teacherRepository.getUnavailableTime(teacherScoreBean.getId(), date);
			for (Object[] time : times) {
				Integer begin = (Integer) time[1];
				Integer end = (Integer) time[2];

				if ((timeEnd >= begin && timeEnd <= end) || (timeBegin >= begin && timeBegin <= end)
						|| (timeBegin <= begin && timeEnd >= end)) {
					// 时间有冲突
					teacherScoreBeanIterator.remove();
					break;
				}
			}
		}

		intelligenceHandler.schedule(null, teacherScoreBeans, date, timeBegin, timeEnd);//规则筛选 一天最多两节 第一天晚上上课 第二天早起不排课
        for(TeacherScoreBean bean:teacherScoreBeans){
            Integer score = bean.getScore();//取出评分
            if(ids!=null&&!ids.isEmpty()){//推荐教师加分
                if(ids.contains(BigInteger.valueOf(bean.getId()))){//如果推荐教师集合有此id
                    score+=50;//推荐教师+50分
                    bean.setScore(score);
                };
            };
        }
        teacherScoreBeans.sort((o1,o2)-> o2.getScore()-o1.getScore());
		return teacherScoreBeans;
	}

	@Override
	public List<Teacher> findByTeacherTypeAndStatus(TeacherType teacherType, TeacherStatus teacherStatus) {
		return teacherRepository.findByTeacherTypeAndStatus(teacherType, teacherStatus);
	}

    @Override
    public List<TeacherScoreBean> getAvailableAssistant(Date date, Integer timeBegin, Integer timeEnd, TeacherType teacherType) {
		List<Long> ids=null;
		switch (teacherType){//不同集合查找不同ids
			case ZJ:
				ids= teacherRepository.getUnavailableAssistantIds(date, timeBegin, timeEnd);
				break;
			case ZCR:
				ids= teacherRepository.getUnavailableCompereIds(date, timeBegin, timeEnd);
				break;
			case CK:
				ids= teacherRepository.getUnavailableControllerIds(date, timeBegin, timeEnd);
				break;
			case XXS:
				ids= teacherRepository.getUnavailableLearningTeacherIds(date, timeBegin, timeEnd);
				break;
			default://TODO查询空闲状态讲师
		}
		List<Long> idList=ids;
        List<Long> offRecordIds = teacherRepository.getOffRecordIds(date, timeBegin, timeEnd);//请假教师id
        idList.addAll(offRecordIds);
        idList.remove(null);//移除not in集合中null元素
		Specification<Teacher> querySpecific = new Specification<Teacher>() {
			@Override
			public Predicate toPredicate(Root<Teacher> root, CriteriaQuery<?> criteriaQuery,
										 CriteriaBuilder criteriaBuilder) {
				List<Predicate> predicates = new ArrayList<>();
				if (null!=idList&&!idList.isEmpty()) {//ids有不为null元素添加判断条件
					predicates.add(root.get("id").in(idList).not());
				}
				if (teacherType != null) {//查找指定类型
					predicates.add(criteriaBuilder.equal(root.get("teacherType"), teacherType));
				}
				predicates.add(criteriaBuilder.equal(root.get("status"), TeacherStatus.ZC));//正常状态
				return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		};
        List<Teacher> teachers = teacherRepository.findAll(querySpecific);
        List<TeacherScoreBean> teacherScoreBeans = Lists.newArrayList();
        teachers.forEach(teacher -> {
            TeacherScoreBean teacherScoreBean = new TeacherScoreBean();
            teacherScoreBean.setId(teacher.getId());
            teacherScoreBean.setName(teacher.getName());

            teacherScoreBeans.add(teacherScoreBean);
        });
		Iterator<TeacherScoreBean> teacherScoreBeanIterator = teacherScoreBeans.iterator();
		while (teacherScoreBeanIterator.hasNext()) {
			TeacherScoreBean teacherScoreBean = teacherScoreBeanIterator.next();

			// 获取上课时间和请假时间
			List<Object[]> times = teacherRepository.getUnavailableTime(teacherScoreBean.getId(), date);
			for (Object[] time : times) {
				Integer begin = (Integer) time[1];
				Integer end = (Integer) time[2];

				if ((timeEnd >= begin && timeEnd <= end) || (timeBegin >= begin && timeBegin <= end)
						|| (timeBegin <= begin && timeEnd >= end)) {
					// 时间有冲突
					teacherScoreBeanIterator.remove();
					break;
				}
			}
		}
		return teacherScoreBeans;
    }

	@Override
	public Teacher findByPhone(String phone) {
		return teacherRepository.findByPhone(phone);
	}

	@Override
	public Set<String> getAuthorities(Long id) {
		return teacherRepository.getAuthorities(id);
	}

	@Override
	public List<Teacher> findByExamTypeAndSubjectId(ExamType examType, Long subjectId) {
		return teacherRepository.findByExamTypeAndSubjectId(examType, subjectId);
	}

	@Override
	public PageImpl<TeacherVo> findInterviewTeacher(Pageable page) {
		List leverTeachers = teacherRepository.getlevelTeacherByExamType(ExamType.MS.getId(),TeacherCourseLevel.COMMON.ordinal());//取出符合级别要求ids
		Specification<Teacher> querySpecific = new Specification<Teacher>() {
			@Override
			public Predicate toPredicate(Root<Teacher> root, CriteriaQuery<?> criteriaQuery,
										 CriteriaBuilder criteriaBuilder) {
				List<Predicate> predicates = new ArrayList<>();
				if(leverTeachers!=null&&!leverTeachers.isEmpty()){
					predicates.add(root.get("id").in(leverTeachers));//id在符合面试类型教师id中
				}else{
					predicates.add(root.get("id").in(0));//没有符合要求的直接添加0
				}
				predicates.add(criteriaBuilder.equal(root.get("teacherType"), TeacherType.JS));//讲师类型
				predicates.add(criteriaBuilder.equal(root.get("status"), TeacherStatus.ZC));//正常状态
				return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		};
		Page<Teacher> teachers = teacherRepository.findAll(querySpecific, page);
		List<Teacher> content = teachers.getContent();
		List teacherVos=new ArrayList();
		if(content!=null&&!content.isEmpty()){
			for(Teacher teacher:content) {
				TeacherVo teacherVo = new TeacherVo(teacher);
				teacherVos.add(teacherVo);
			}
		}
		PageImpl<TeacherVo> pageTeacheres=new PageImpl(teacherVos,page,teachers == null ? 0 : teachers.getTotalElements());
		return pageTeacheres;
	}

	@Override
	public List<OptionVo> getRolesById(Long id) {
		List<OptionVo> roleManageVos = Lists.newArrayList();

		teacherRepository.getRolesById(id).forEach(role -> {
			roleManageVos
					.add(new OptionVo(Long.parseLong(role[0].toString()), role[1].toString(), role[2] != null));
		});
		return roleManageVos;
	}

	@Override
	public List<OptionVo> getRolesByIdExclude(Long id) {
		List<OptionVo> roleManageVos = Lists.newArrayList();

		teacherRepository.getRolesById(id).forEach(role -> {
			roleManageVos
					.add(new OptionVo(Long.parseLong(role[0].toString()), role[1].toString(), role[2] != null));
		});
		roleManageVos.remove(7);//去除组长
		roleManageVos.remove(2);//去除教师
		roleManageVos.remove(1);//去除教师
		roleManageVos.remove(0);//去除管理员选项
		return roleManageVos;
	}

	@Override
	@Transactional
	public void updateRolesById(Long id, List<Long> roleIds) {
		// 清空权限
		teacherRepository.clearRolesById(id);

		// 添加权限
		roleIds.forEach(roleId -> {
			teacherRepository.saveRolesById(id, roleId);
		});
	}

	@Transactional
	@Override
	public Boolean updateTeacher(Teacher teacher, List<TeacherSubject> subjects) {
        Long teacherId = teacher.getId();
        List<TeacherSubject> teacherSubjects = teacher.getTeacherSubjects();//新授课集合
        if(teacherSubjects!=null){//将新数据关联teacherid
            teacherSubjects.forEach(subject->{
                subject.setTeacherId(teacherId);
            });
        }else {//新数据空直接删除原数据
            teacherSubjectRepository.deleteByTeacherId(teacherId);
            subjects=new ArrayList();
        }

        for(TeacherSubject teacherSubject:subjects){
            Long oldId = teacherSubject.getId();//旧数据id
            boolean flag=true;
            for(TeacherSubject newTeacherSubject:teacherSubjects){
                if(oldId.equals(newTeacherSubject.getId())){
                    flag=false;
                }
            }
            if(flag){//新数据中没有旧数据id  将旧进行删除
                teacherSubjectRepository.delete(oldId);
            }
        }
        teacherRepository.save(teacher);
        // 清空功能权限
        teacherRepository.clearRolesById(teacherId);
		if(teacher.getTeacherType().equals(TeacherType.JS)){//讲师类型
			if(teacher.getLeaderFlag()){//组长类型
				teacherRepository.saveRolesById(teacherId, 8l);
			}else{//非组长类型
				teacherRepository.saveRolesById(teacherId, 2l);
			}
		}else{//助教类型
			teacherRepository.saveRolesById(teacherId, 3l);
		}
        return true;
	}

	/**
	 * @description: 导入教师课程
	 * @author duanxiangchao
	 * @date 2018/5/15 上午10:37
	 */
	@Override
	@Transactional
	public void importTeacherCourse(List<List<List<String>>> importList, Long courseId) {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
		Integer year = Integer.parseInt(sdf.format(new Date()));

		for(List<List<String>> list: importList){
			if(list.size() > 0){
				List<ImportTeacherCourseBean> beans = Lists.newArrayList();
				Set<String> teacherNames = Sets.newHashSet();
				List<Date> dateList = Lists.newArrayList();

				for(int i = 0; i < list.size(); i++){
					List<String> currentList = list.get(i);
					if(currentList.get(1).trim().equals("时间")){
						dateList.clear();
						dateList.add(null);
						dateList.add(null);
						for(int j = 2; j < currentList.size(); j ++){
							Date date = DateformatUtil.getDateFromSDF5(year + "年" + currentList.get(j));
							dateList.add(date);
						}
					} else {
						String[] strings = currentList.get(1).split("\n");
						String[] times = strings[1].split("-");
						for(int j=2; j < currentList.size(); j++){
							String content = currentList.get(j);
							if(StringUtils.isNotBlank(content)){
								ImportTeacherCourseBean bean = new ImportTeacherCourseBean();
								String[] contents = content.split("\n");
								String courseFullName = contents[0];
								String courseName = courseFullName.substring(0, courseFullName.length()-1);
								String teacherName = contents[1];
								bean.setDate(dateList.get(j));
								bean.setTimeBegin(Integer.parseInt(times[0].trim().replace(":", "")));
								bean.setTimeEnd(Integer.parseInt(times[1].trim().replace(":", "")));
								bean.setCourseName(courseName);
								String st = courseFullName.substring(courseFullName.length()-1, courseFullName.length());
								bean.setCoursePhase(CoursePhase.create(Integer.parseInt(courseFullName.substring(courseFullName.length()-1, courseFullName.length()))));
								bean.setTeacherName(teacherName);
								teacherNames.add(teacherName);
								beans.add(bean);
							}
						}
					}
				}
				Map<String, Long> teacherMap = validateTeacher(teacherNames);
				Course course = courseRepository.findOne(courseId);
				batchAddTeacherCourse(beans, course, teacherMap);
//			((TeacherServiceImpl)AopContext.currentProxy()).batchAddTeacherCourse(beans, courseId, teacherMap);
			}
		}
	}

	/**
	 * @description: 校验教师的合法性
	 * @author duanxiangchao
	 * @date 2018/5/14 下午4:35
	 */
	private Map<String, Long> validateTeacher(Set<String> teacherNames){
		List<Teacher> teacherList = teacherRepository.findByNameIn(teacherNames);
		if(teacherList.size() != teacherNames.size()){
			//教研不存在的教师
			Set<String> containTeacherName = Sets.newHashSet();
			teacherList.forEach(teacher -> {
				containTeacherName.add(teacher.getName());
			});
			teacherNames.forEach(teacherName -> {
				if(!containTeacherName.contains(teacherName)){
					throw new BadRequestException("教师" + teacherName + "不存在，请先添加教师");
				}
			});
		} else {
			Map<String, Long> teacherMap = Maps.newHashMap();
			teacherList.forEach(teacher -> {
				teacherMap.put(teacher.getName(), teacher.getId());
			});
			return teacherMap;
		}
		return null;
	}


	/**
	 * @description: 导入非滚动排课
	 * @author duanxiangchao
	 * @date 2018/5/14 下午4:35
	 */
	public void batchAddTeacherCourse(List<ImportTeacherCourseBean> beans, Course course, Map<String, Long> teacherMap){

		LiveRoom liveRoom = liveRoomRepository.findOneByName("直播间-1");
		beans.forEach(courseBean -> {
			if(courseBean != null){
				Long teacherId = teacherMap.get(courseBean.getTeacherName());
				List<Object> teacherSubjectList = teacherSubjectRepository.findBySubjectAndTeacher(course.getSubjectId(), teacherId);
				TeacherCourseLevel teacherCourseLevel = null;
				if(teacherSubjectList.size() != 0){
					teacherCourseLevel = TeacherCourseLevel.create(Integer.parseInt(teacherSubjectList.get(0).toString()));
				}
				//添加直播
				CourseLive courseLive = courseLiveWrapper(courseBean, course.getId(), liveRoom.getId());
				courseLiveRepository.save(courseLive);
				courseBean.setCourseLiveId(courseLive.getId());
				//添加教师直播关联
				CourseLiveTeacher courseLiveTeacher = courseLiveTeacherWrapper(courseLive.getId(), teacherId,
						courseBean.getCoursePhase(), teacherCourseLevel);
				courseLiveTeacherRepository.save(courseLiveTeacher);
			}
		});
	}


	/**
	 * @description: 导入滚动排课
	 * @author duanxiangchao
	 * @date 2018/5/14 下午4:34
	 */
	public void importTeacherRollCourse(List<List<List<String>>> importList){
		for(int k = 0; k < 1; k++){
			//sheet页遍历
			List<List<ImportTeacherCourseBean>> lists = new ArrayList<List<ImportTeacherCourseBean>>();
			lists.add(null);
			lists.add(null);
			List<List<String>> sheetList = importList.get(k);
			Set<String> teacherNames = getTeacherNames(sheetList);
			Map<String, Long> teacherMap = validateTeacher(teacherNames);
			Date lastDate = null;
			for(int i = 1; i < sheetList.size(); i++){
				//从第二行开始，获取单行数据
				List<String> rowData = sheetList.get(i);
				//获取第一列日期
				String dateStr = rowData.get(0);
				if(StringUtils.isNotBlank(dateStr)){
					Date currentDate = null;
					currentDate = HSSFDateUtil.getJavaDate(Double.parseDouble(rowData.get(0)));
					int countRow = 1;
					if(currentDate.equals(lastDate)){
						//当天有两节课
						countRow = 2;
					}
					lastDate = currentDate;
					//获取课程时间
					WeekEnum weekEnum = WeekEnum.create(rowData.get(1).trim());
					ScheduleTimeEnum scheduleTimeEnum = ScheduleTimeEnum.getWeekScheduleTimeEnum(weekEnum, countRow);
					//时间日期获取完毕，开始遍历数据
					String lastStr = "";
					for(int j = 2; j < rowData.size(); j++){
						String dataStr = rowData.get(j);
						if(StringUtils.isNotBlank(dataStr)){
							//获取列bean集合
							List<ImportTeacherCourseBean> beans = null;
							if(lists.size() == j){
								//集合没有初始化
								beans = new ArrayList<ImportTeacherCourseBean>();
								lists.add(beans);
							} else if(lists.size() < j){
								lists.add(null);
								beans = new ArrayList<ImportTeacherCourseBean>();
								lists.add(beans);
							} else {
								beans = lists.get(j);
							}
							ImportTeacherCourseBean courseBean = new ImportTeacherCourseBean();
							courseBean.setDate(currentDate);
							courseBean.setTimeBegin(scheduleTimeEnum.getTimeBegin());
							courseBean.setTimeEnd(scheduleTimeEnum.getTimeEnd());
							String coursePhaseStr = dataStr.replaceAll("[^0-9]", "");
							if(StringUtils.isNotBlank(coursePhaseStr)){
								courseBean.setCoursePhase(CoursePhase.create(Integer.parseInt(coursePhaseStr)));
							}
							String[] strs = dataStr.split(coursePhaseStr);
							courseBean.setCourseName(strs[0] + coursePhaseStr);
							courseBean.setTeacherName(strs[1]);
							beans.add(courseBean);
							if(dataStr.equals(lastStr)){
								//滚动排课
								try{
									List<ImportTeacherCourseBean> preBeans = lists.get(j - 1);
									courseBean.setRoll(true);
									courseBean.setTeacherCourseBean(getTeacherCourseBean(preBeans.get(preBeans.size() - 1)));
								} catch (Exception e){
									LOG.error("错误数据： lastStr:" + lastStr + "dataStr:" + dataStr );
									throw e;
								}

							}
							lastStr = dataStr;
						} else {
							lastStr = dataStr;
						}
					}
				}
			}

			int count = 1;
			Date now = new Date();
			String nowStr = DateformatUtil.format6(now);
			for(int i = 0; i < lists.size(); i++){
				if(lists.get(i) != null){
					Course course = new Course();
					course.setName("导入" + nowStr + "_" + (count++));
					Date currentDate = new Date();
					course.setDateBegin(new Date());
					course.setAssistantFlag(false);
					course.setCompereFlag(false);
					course.setControllerFlag(false);
					course.setLearningTeacherFlag(false);
					course.setSatFlag(false);
					course.setSunFlag(false);
					course.setStatus(CourseStatus.ZBAP);
					course.setDateEnd(DateUtils.addDays(currentDate, 60));
					course.setCourseCategory(CourseCategory.LIVE);
					course.setExamType(ExamType.GWY);
					courseRepository.save(course);
					batchAddTeacherCourse(lists.get(i), course, teacherMap);
				}
			}
		}
	}



	/**
	 * @description: 获取source
	 * @author duanxiangchao
	 * @date 2018/5/15 下午6:53
	 */
	public ImportTeacherCourseBean getTeacherCourseBean(ImportTeacherCourseBean courseBean){
		if(courseBean.isRoll()){
			return getTeacherCourseBean(courseBean.getTeacherCourseBean());
		} else {
			return courseBean;
		}
	}

	/**
	 * @description: 获取单个sheet页的所有教师
	 * @author duanxiangchao
	 * @date 2018/5/15 上午10:30
	 */
	private Set<String> getTeacherNames(List<List<String>> dataList) {
		Set<String> teacherNames = Sets.newHashSet();
		for(int i = 1; i < dataList.size(); i++){
			//第二行开始遍历
			List<String> strList = dataList.get(i);
			for(int j = 2; j < strList.size(); j++){
				//第三列开始遍历
				String str = strList.get(j);
				if(StringUtils.isNotBlank(str)){
					try {
						String num = str.replaceAll("[^0-9]", "");
						String[] strings = str.split(num);
						teacherNames.add(strings[1].trim());
					} catch (Exception e){
						LOG.error("错误数据：" + str);
						throw new BadRequestException("Excel内容错误：" + str);
					}

				}
			}
		}
		return teacherNames;
	}


	public List<OptionVo> getDataPermissionsById(Long id) {
		List<OptionVo> dataPermissionVos = Lists.newArrayList();

		for(ExamType examType:ExamType.values()){
			dataPermissionVos.add(new OptionVo(Long.parseLong(examType.getId() + ""),examType.getText(), false));
		}
		Set<Integer> dataPermissionIds = teacherRepository.findDataPermissionIdsById(id);

		dataPermissionVos.forEach(dataPermissionVo -> {
			if (dataPermissionIds.contains(dataPermissionVo.getId().intValue())) {
				dataPermissionVo.setChecked(true);
			}
		});

		return dataPermissionVos;
	}

	@Override
	@Transactional
	public void updatePermissionsById(Long id, List<List<Long>> permissionIds) {
		// 清空功能权限
		teacherRepository.clearRolesById(id);//不清除管理员权限

		// 添加功能权限
		permissionIds.get(0).forEach(roleId -> {
			teacherRepository.saveRolesById(id, roleId);
		});

		// 清空数据权限
		teacherRepository.clearDataPermissionsById(id);

		// 添加数据权限
		permissionIds.get(1).forEach(roleId -> {
			teacherRepository.saveDataPermissionsById(id, roleId);
		});

	}

	@Override
	public Set<ExamType> findDataPermissionIdsById(Long id) {
		Set<Integer> dataPermissionIds = teacherRepository.findDataPermissionIdsById(id);
		Set<ExamType> dataPermissions = Sets.newHashSet();

		dataPermissionIds.forEach(dataPermissionId -> {
			dataPermissions.add(ExamType.findById(dataPermissionId));
		});
		return dataPermissions;
	}

	@Override
	public Teacher findByPid(Long pid) {
		return teacherRepository.findByPid(pid);
	}

	@Override
	@Transactional
	public void importTeachers(List<List<List<String>>> list) {
		for(List<List<String>> sheetList:list){//sheet
			for(List<String> data:sheetList){//一条教师数据
				Teacher teacher=new Teacher();
				teacher.setName(data.get(1));
				teacher.setWechat(data.get(2));
				teacher.setPhone(data.get(3));
				teacher.setExamType(ExamType.GWY);//默认公务员
				teacher.setTeacherType(TeacherType.JS);//默认讲师
				try {
					teacherRepository.save(teacher);
				}catch (Exception e){
					LOG.error("插入失败:"+teacher.getPhone().toString());
				}
			}
		}
	}

	@Override
	public void importTeacherByPHP(List<PHPUpdateTeacherDto> data) {
		for(PHPUpdateTeacherDto teacherDto:data) {
			try {
				Teacher teacher=new Teacher();

				Integer status = teacherDto.getStatus();
				if(status==null||status==0){//设置状态
					teacher.setStatus(TeacherStatus.DSH);
				}else if(status==1){
					teacher.setStatus(TeacherStatus.ZC);
				}else {
					teacher.setStatus(TeacherStatus.JY);
				}
				String phone = teacherDto.getPhone();
				if("".equals(phone)){//处理空字符串
					phone=null;
				}
				teacher.setPhone(phone);//设置电话
				teacher.setName(teacherDto.getName());//设置名字
				teacher.setPid(teacherDto.getPid());//设置phpid
				Long subjectId = teacherDto.getSubjectId();//设置科目
				teacher.setSubjectId(subjectId);
				Integer examTypeId = teacherDto.getExamType();//考试类型id
				ExamType examTypeById=null;
				if(examTypeId!=null){//考试类型有值
					examTypeById = ExamType.findById(examTypeId);//id转化成类型
					teacher.setExamType(examTypeById);//设置考试类型

				}
				teacher.setTeacherType(TeacherType.JS);
				Teacher save = teacherRepository.save(teacher);
				Long id = save.getId();
				if(examTypeById!=null||subjectId!=null){//考试类型或科目不为空 创建
					TeacherSubject ts=new TeacherSubject();
					ts.setTeacherId(id);
					ts.setExamType(examTypeById);
					ts.setSubjectId(subjectId);
					ts.setTeacherCourseLevel(TeacherCourseLevel.COMMON);
					teacherSubjectRepository.save(ts);
				}
			}catch (Exception e){
				LOG.error("插入失败:"+teacherDto.toString());
				LOG.error("失败原因:"+e.getMessage());
				LOG.info("发起失败重试,去除手机号码");
				Teacher teacher=new Teacher();

				Integer status = teacherDto.getStatus();
				if(status==null||status==0){//设置状态
					teacher.setStatus(TeacherStatus.DSH);
				}else if(status==1){
					teacher.setStatus(TeacherStatus.ZC);
				}else {
					teacher.setStatus(TeacherStatus.JY);
				}
				teacher.setName(teacherDto.getName());//设置名字
				teacher.setPid(teacherDto.getPid());//设置phpid
				Long subjectId = teacherDto.getSubjectId();//设置科目
				teacher.setSubjectId(subjectId);
				Integer examTypeId = teacherDto.getExamType();//考试类型id
				ExamType examTypeById=null;
				if(examTypeId!=null){//考试类型有值
					examTypeById = ExamType.findById(examTypeId);//id转化成类型
					teacher.setExamType(examTypeById);//设置考试类型

				}
				Teacher save = teacherRepository.save(teacher);
				Long id = save.getId();
				if(examTypeById!=null||subjectId!=null){//考试类型或科目不为空 创建
					TeacherSubject ts=new TeacherSubject();
					ts.setTeacherId(id);
					ts.setExamType(examTypeById);
					ts.setSubjectId(subjectId);
					ts.setTeacherCourseLevel(TeacherCourseLevel.COMMON);
					teacherSubjectRepository.save(ts);
				}
			}
		}
	}

	@Override
	public int updateStatusByPids(List<Long> pids, Integer status) {
		return teacherRepository.updateStatusByPids(pids,status);
	}

	private CourseLive courseLiveWrapper(ImportTeacherCourseBean bean, Long courseId, Long liveRoomId){
		CourseLive courseLive = new CourseLive();
		courseLive.setCourseId(courseId);
		courseLive.setName(bean.getCourseName());
		courseLive.setDate(bean.getDate());
		courseLive.setTimeBegin(bean.getTimeBegin());
		courseLive.setTimeEnd(bean.getTimeEnd());
		courseLive.setLiveRoomId(liveRoomId);
		courseLive.setAssConfirm(CourseConfirmStatus.DQR);
		courseLive.setComConfirm(CourseConfirmStatus.DQR);
		courseLive.setCtrlConfirm(CourseConfirmStatus.DQR);
		courseLive.setLtConfirm(CourseConfirmStatus.DQR);
		courseLive.setDateInt(Integer.parseInt(DateformatUtil.format1(bean.getDate())));
		if(bean.isRoll()){
			courseLive.setSourceId(bean.getTeacherCourseBean().getCourseLiveId());
		}
		return courseLive;
	}

	private CourseLiveTeacher courseLiveTeacherWrapper(Long courseLiveId, Long teacherId, CoursePhase coursePhase, TeacherCourseLevel teacherCourseLevel){
		CourseLiveTeacher courseLiveTeacher = new CourseLiveTeacher();
		courseLiveTeacher.setCourseLiveId(courseLiveId);
		courseLiveTeacher.setTeacherId(teacherId);
		courseLiveTeacher.setConfirm(CourseConfirmStatus.QR);
		courseLiveTeacher.setCoursePhase(coursePhase);
		courseLiveTeacher.setTeacherCourseLevel(teacherCourseLevel);
		return courseLiveTeacher;
	}

}
