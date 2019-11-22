package com.huatu.tiku.schedule.biz.service.imple;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.huatu.tiku.schedule.base.exception.BadRequestException;
import com.huatu.tiku.schedule.base.service.impl.BaseServiceImpl;
import com.huatu.tiku.schedule.biz.bean.TeacherScoreBean;
import com.huatu.tiku.schedule.biz.domain.*;
import com.huatu.tiku.schedule.biz.enums.*;
import com.huatu.tiku.schedule.biz.repository.*;
import com.huatu.tiku.schedule.biz.service.TeacherService;
import com.huatu.tiku.schedule.biz.service.intelligence.IntelligenceHandler;
import com.huatu.tiku.schedule.biz.util.DateformatUtil;
import com.huatu.tiku.schedule.biz.util.TimeRangeUtil;
import com.huatu.tiku.schedule.biz.vo.OptionVo;
import com.huatu.tiku.schedule.biz.vo.TeacherVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.criteria.*;
import java.math.BigInteger;
import java.util.*;

@Service
@EnableAspectJAutoProxy
public class TeacherServiceImpl extends BaseServiceImpl<Teacher, Long> implements TeacherService {


    Logger LOG = LoggerFactory.getLogger(TeacherServiceImpl.class);

    private final TeacherRepository teacherRepository;
    private final TeacherSubjectRepository teacherSubjectRepository;
    private final OffRecordRepository offRecordRepository;
    @Resource
    private IntelligenceHandler intelligenceHandler;

    @Autowired
    public TeacherServiceImpl(TeacherRepository teacherRepository, TeacherSubjectRepository teacherSubjectRepository, OffRecordRepository offRecordRepository) {
        this.teacherRepository = teacherRepository;
        this.teacherSubjectRepository = teacherSubjectRepository;
        this.offRecordRepository = offRecordRepository;
    }

    @Override
    @Transactional
    public Teacher saveX(Teacher teacher, List<TeacherSubject> teacherSubjects) {
        boolean exists = teacherRepository.existsByPhone(teacher.getPhone());
        if (exists) {
            throw new BadRequestException("该手机号码已注册");
        }
        teacher = teacherRepository.save(teacher);
        Long teacherId = teacher.getId();//主键id
        if (teacherSubjects != null && !teacherSubjects.isEmpty()) {
            Iterator<TeacherSubject> iterator = teacherSubjects.iterator();
            while (iterator.hasNext()) {//添加主键
                TeacherSubject subject = iterator.next();
                if (subject.getExamType() == null || subject.getSubjectId() == null || subject.getTeacherCourseLevel() == null) {//有一个为空删除
                    iterator.remove();
                } else {//都不为空添加主键
                    subject.setTeacherId(teacherId);
                }
            }
            teacherSubjectRepository.save(teacherSubjects);
        }
        if (teacher.getTeacherType().equals(TeacherType.JS)) {//讲师类型
            if (teacher.getLeaderFlag()) {//组长类型
                teacherRepository.saveRolesById(teacherId, 8l);
            } else {//非组长类型
                teacherRepository.saveRolesById(teacherId, 2l);
            }
        } else {//助教类型
            teacherRepository.saveRolesById(teacherId, 3l);
        }
        return teacher;
    }

    @Override
    public Page<TeacherVo> getTeacherList(ExamType examType, String name, Long id, Long subjectId,
                                          Boolean leaderFlag, TeacherStatus status, TeacherType teacherType, Pageable page) {

        Specification<Teacher> querySpecific = new Specification<Teacher>() {
            @Override
            public Predicate toPredicate(Root<Teacher> root, CriteriaQuery<?> criteriaQuery,
                                         CriteriaBuilder criteriaBuilder) {

                List<Predicate> predicates = new ArrayList<>();
                if (examType != null) {
                    predicates.add(criteriaBuilder.equal(root.get("examType"), examType));
                }
                if (!Strings.isNullOrEmpty(name)) {
                    predicates.add(criteriaBuilder.like(root.get("name"), "%" + name + "%"));
                }
                if (id != null) {
                    predicates.add(criteriaBuilder.equal(root.get("id"), id));
                }
                if (subjectId != null) {
                    predicates.add(criteriaBuilder.equal(root.get("subjectId"), subjectId));
                }
                if (leaderFlag != null) {
                    predicates.add(criteriaBuilder.equal(root.get("leaderFlag"), leaderFlag));
                }
                if (status != null) {
                    predicates.add(criteriaBuilder.equal(root.get("status"), status));
                }
                if (teacherType != null) {
                    if (teacherType.equals(TeacherType.JS)) {//讲师类型
                        predicates.add(criteriaBuilder.equal(root.get("teacherType"), TeacherType.JS));
                    } else {
                        predicates.add(criteriaBuilder.notEqual(root.get("teacherType"), TeacherType.JS));
                    }
                }

                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        List<Sort.Order> list = new ArrayList();
        list.add(new Sort.Order(Sort.Direction.ASC, "status"));//状态正旭
        list.add(new Sort.Order(Sort.Direction.DESC, "id"));//id倒序
        Pageable pageable = new PageRequest(page.getPageNumber(), page.getPageSize(), new Sort(list));
        Page<Teacher> teachers = teacherRepository.findAll(querySpecific, pageable);
        List<Teacher> content = teachers.getContent();
        List<TeacherVo> teacherVos = new ArrayList();
        if (content != null && !content.isEmpty()) {
            for (Teacher teacher : content) {
                TeacherVo teacherVo = new TeacherVo(teacher);
                teacherVos.add(teacherVo);
            }
        }
        return new PageImpl(teacherVos, page, teachers == null ? 0 : teachers.getTotalElements());
    }

    @Override
    public int updateTeacherStatus(List<Long> ids, TeacherStatus status, Long id) {
        return teacherRepository.updateTeacherStatus(ids, status, id);
    }

    @Override
    public List<TeacherScoreBean> getAvailableTeachers(Date date, Integer timeBegin, Integer timeEnd, ExamType examType,
                                                       Long subjectId, TeacherCourseLevel teacherCourseLevel, Long courseId) {
        List<Teacher> teachers = null;
        List<Long> ids = null;
        if (courseId != null && ExamType.MS.equals(examType)) {//面试类型 先去查找推荐教师
            ids = teacherRepository.findTeacherByCourseId(courseId);
        }
        // 获取所有符合条件的教师
        teachers = teacherRepository.findAll(new Specification<Teacher>() {
            @Override
            public Predicate toPredicate(Root<Teacher> root, CriteriaQuery<?> criteriaQuery,
                                         CriteriaBuilder criteriaBuilder) {
                criteriaQuery.distinct(true);

                List<Predicate> predicates = new ArrayList<>();

                Join<Teacher, TeacherSubject> teacherSubjects = root.join("teacherSubjects");

                 if (subjectId != null) {
                    predicates.add(criteriaBuilder.equal(teacherSubjects.get("subjectId"), subjectId));
                } else if (examType != null) {
                    predicates.add(criteriaBuilder.equal(teacherSubjects.get("examType"), examType));
                }

                if (teacherCourseLevel != null) {
                    predicates.add(criteriaBuilder.equal(teacherSubjects.get("teacherCourseLevel"), teacherCourseLevel));
                }

                // 审核状态
                predicates.add(criteriaBuilder.equal(root.get("status"), TeacherStatus.ZC));
                // 教师类型
//                predicates.add(criteriaBuilder.equal(root.get("teacherType"), TeacherType.JS));


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
        String dateString = DateformatUtil.format0(date);
        Date queryTimeBegin=DateformatUtil.parse8(dateString+" "+TimeRangeUtil.intToDateString(timeBegin));
        Date queryTimeEnd=DateformatUtil.parse8(dateString+" "+TimeRangeUtil.intToDateString(timeEnd));
        while (teacherScoreBeanIterator.hasNext()) {
            TeacherScoreBean teacherScoreBean = teacherScoreBeanIterator.next();
            List<OffRecord> offrecordResult = offRecordRepository.findByTeacherIdAndDate(teacherScoreBean.getId(), queryTimeBegin, queryTimeEnd);
            if(null!=offrecordResult&&!offrecordResult.isEmpty()){
                teacherScoreBeanIterator.remove();
                continue;
            }
            // 获取上课时间和请假时间
            List<Object[]> times = teacherRepository.getUnavailableTime(teacherScoreBean.getId(), date);
            for (Object[] time : times) {
                Integer begin = (Integer) time[1];
                Integer end = (Integer) time[2];

                if ((timeEnd > begin && timeEnd < end) || (timeBegin > begin && timeBegin < end)
                        || (timeBegin <= begin && timeEnd >= end)) {
                    // 时间有冲突
                    teacherScoreBeanIterator.remove();
                    break;
                }
            }

        }
        //对面试教师排序
        if (ids != null && !ids.isEmpty()) {
            for (TeacherScoreBean bean : teacherScoreBeans) {
                if (ids.contains(BigInteger.valueOf(bean.getId()))) {//如果推荐教师集合有此id
                    Integer score = bean.getScore();//取出评分
                    score += 50;//推荐教师+50分
                    bean.setScore(score);
                }
                ;
            }
            ;
        }
        teacherScoreBeans.sort((o1, o2) -> o2.getScore() - o1.getScore());

        return teacherScoreBeans;
    }

    @Override
    public List<TeacherScoreBean> getAvailableCtrl(Date date, Integer timeBegin, Integer timeEnd, TeacherType teacherType) {
        // 获取所有符合条件的教师
        List<Teacher> teachers = teacherRepository.findAll(new Specification<Teacher>() {
            @Override
            public Predicate toPredicate(Root<Teacher> root, CriteriaQuery<?> criteriaQuery,
                                         CriteriaBuilder criteriaBuilder) {
                criteriaQuery.distinct(true);
                List<Predicate> predicates = new ArrayList<>();
                // 审核状态
                predicates.add(criteriaBuilder.equal(root.get("status"), TeacherStatus.ZC));
                // 教师类型
                predicates.add(criteriaBuilder.equal(root.get("teacherType"), teacherType));
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
        String dateString = DateformatUtil.format0(date);
        Date queryTimeBegin=DateformatUtil.parse8(dateString+" "+TimeRangeUtil.intToDateString(timeBegin));
        Date queryTimeEnd=DateformatUtil.parse8(dateString+" "+TimeRangeUtil.intToDateString(timeEnd));
        while (teacherScoreBeanIterator.hasNext()) {
            TeacherScoreBean teacherScoreBean = teacherScoreBeanIterator.next();
            // 获取上课时间和请假时间
            List<OffRecord> offrecordResult = offRecordRepository.findByTeacherIdAndDate(teacherScoreBean.getId(), queryTimeBegin, queryTimeEnd);
            if(null!=offrecordResult&&!offrecordResult.isEmpty()){
                teacherScoreBeanIterator.remove();
                continue;
            }
            List<Object[]> times = teacherRepository.getUnavailableTime(teacherScoreBean.getId(), date);
            for (Object[] time : times) {
                Integer begin = (Integer) time[1];
                Integer end = (Integer) time[2];

                if ((timeEnd > begin && timeEnd < end) || (timeBegin > begin && timeBegin < end)
                        || (timeBegin <= begin && timeEnd >= end)) {
                    // 时间有冲突
                    teacherScoreBeanIterator.remove();
                    break;
                }
            }
        }
        teacherScoreBeans.sort((o1, o2) -> o2.getScore() - o1.getScore());
        return teacherScoreBeans;
    }

    @Override
    public List<TeacherScoreBean> autoGetAvailableTeachers(Date date, Integer timeBegin, Integer timeEnd, ExamType examType,
                                                           Long subjectId, TeacherCourseLevel teacherCourseLevel, Long courseId,
                                                           Boolean flag) {
        List<Teacher> teachers = null;
        List<Long> ids = null;
        if (courseId != null && examType.equals(ExamType.MS)) {//面试类型 先去查找推荐教师
            ids = teacherRepository.findTeacherByCourseId(courseId);
        }
        if (teacherCourseLevel == null) {//授课级别空 添加默认等级
            if(flag) {
                teacherCourseLevel = TeacherCourseLevel.COMMON;
            }else {
                teacherCourseLevel = TeacherCourseLevel.ZZZJ;
            }
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

                if (subjectId != null) {
                    predicates.add(criteriaBuilder.equal(teacherSubjects.get("subjectId"), subjectId));
                } else if (examType != null) {
                    predicates.add(criteriaBuilder.equal(teacherSubjects.get("examType"), examType));
                }
                if (finalTeacherCourseLevel != null) {
                    if(flag){
                        predicates
                                .add(criteriaBuilder.greaterThanOrEqualTo(teacherSubjects.get("teacherCourseLevel"), finalTeacherCourseLevel));
                    }else{
                        predicates
                                .add(criteriaBuilder.equal(teacherSubjects.get("teacherCourseLevel"), finalTeacherCourseLevel));
                    }
                }
                // 审核状态
                predicates.add(criteriaBuilder.equal(root.get("status"), TeacherStatus.ZC));

                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        });

        List<TeacherScoreBean> teacherScoreBeans = Lists.newArrayList();
        teachers.forEach(teacher -> {
            TeacherScoreBean teacherScoreBean = new TeacherScoreBean();
            teacherScoreBean.setId(teacher.getId());
            teacherScoreBean.setName(teacher.getName());
            teacherScoreBean.setScore(0);
            teacher.getTeacherSubjects().forEach(subject -> {
                if (subjectId != null) {
                    if (subjectId.equals(subject.getSubjectId())) {
                        if (finalTeacherCourseLevel.equals(subject.getTeacherCourseLevel())) {
                            teacherScoreBean.setScore(60);
                        }
                    }
                }
            });
            teacherScoreBeans.add(teacherScoreBean);
        });

        Iterator<TeacherScoreBean> teacherScoreBeanIterator = teacherScoreBeans.iterator();
        String dateString = DateformatUtil.format0(date);
        Date queryTimeBegin=DateformatUtil.parse8(dateString+" "+TimeRangeUtil.intToDateString(timeBegin));
        Date queryTimeEnd=DateformatUtil.parse8(dateString+" "+TimeRangeUtil.intToDateString(timeEnd));
        while (teacherScoreBeanIterator.hasNext()) {
            TeacherScoreBean teacherScoreBean = teacherScoreBeanIterator.next();
            // 获取上课时间和请假时间
            List<OffRecord> offrecordResult = offRecordRepository.findByTeacherIdAndDate(teacherScoreBean.getId(), queryTimeBegin, queryTimeEnd);
            if(null!=offrecordResult&&!offrecordResult.isEmpty()){
                teacherScoreBeanIterator.remove();
                continue;
            }
            List<Object[]> times = teacherRepository.getUnavailableTime(teacherScoreBean.getId(), date);
            for (Object[] time : times) {
                Integer begin = (Integer) time[1];
                Integer end = (Integer) time[2];

                if ((timeEnd > begin && timeEnd < end) || (timeBegin > begin && timeBegin < end)
                        || (timeBegin <= begin && timeEnd >= end)) {
                    // 时间有冲突
                    teacherScoreBeanIterator.remove();
                    break;
                }
            }

        }

        intelligenceHandler.schedule(null, teacherScoreBeans, date, timeBegin, timeEnd);//规则筛选 一天最多两节 第一天晚上上课 第二天早起不排课
        for (TeacherScoreBean bean : teacherScoreBeans) {
            Integer score = bean.getScore();//取出评分
            if (ids != null && !ids.isEmpty()) {//推荐教师加分
                if (ids.contains(BigInteger.valueOf(bean.getId()))) {//如果推荐教师集合有此id
                    score += 50;//推荐教师+50分
                    bean.setScore(score);
                }
            }
        }
        teacherScoreBeans.sort((o1, o2) -> o2.getScore() - o1.getScore());//根据评分排序
        return teacherScoreBeans;
    }

    @Override
    public List<Teacher> findByTeacherTypeAndStatus(TeacherType teacherType, TeacherStatus teacherStatus) {
        return teacherRepository.findByTeacherTypeAndStatus(teacherType, teacherStatus);
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
    public PageImpl<TeacherVo> findInterviewTeacher(Pageable page) {
        List leverTeachers = teacherRepository.getlevelTeacherByExamType(ExamType.MS.getId(), TeacherCourseLevel.COMMON.ordinal());//取出符合级别要求ids
        Specification<Teacher> querySpecific = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (leverTeachers != null && !leverTeachers.isEmpty()) {
                predicates.add(root.get("id").in(leverTeachers));//id在符合面试类型教师id中
            } else {
                predicates.add(root.get("id").in(0));//没有符合要求的直接添加0
            }
//                predicates.add(criteriaBuilder.equal(root.get("teacherType"), TeacherType.JS));//讲师类型
            predicates.add(criteriaBuilder.equal(root.get("status"), TeacherStatus.ZC));//正常状态
            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
        Page<Teacher> teachers = teacherRepository.findAll(querySpecific, page);
        List<Teacher> content = teachers.getContent();
        List teacherVos = new ArrayList();
        if (content != null && !content.isEmpty()) {
            for (Teacher teacher : content) {
                TeacherVo teacherVo = new TeacherVo(teacher);
                teacherVos.add(teacherVo);
            }
        }
        PageImpl<TeacherVo> pageTeacheres = new PageImpl(teacherVos, page, teachers == null ? 0 : teachers.getTotalElements());
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
        if (teacherSubjects != null) {//将新数据关联teacherid
            teacherSubjects.forEach(subject -> {
                subject.setTeacherId(teacherId);
            });
        } else {//新数据空直接删除原数据
            teacherSubjectRepository.deleteByTeacherId(teacherId);
            subjects = new ArrayList();
        }

        for (TeacherSubject teacherSubject : subjects) {
            Long oldId = teacherSubject.getId();//旧数据id
            boolean flag = true;
            for (TeacherSubject newTeacherSubject : teacherSubjects) {
                if (oldId.equals(newTeacherSubject.getId())) {
                    flag = false;
                }
            }
            if (flag) {//新数据中没有旧数据id  将旧进行删除
                teacherSubjectRepository.delete(oldId);
            }
        }
        teacherRepository.save(teacher);
        // 清空功能权限
        teacherRepository.deleteRolesById(teacherId);
        if (teacher.getTeacherType().equals(TeacherType.JS)) {//讲师类型
            if (teacher.getLeaderFlag()) {//组长类型
                teacherRepository.saveRolesById(teacherId, 8l);
            } else {//非组长类型
                teacherRepository.saveRolesById(teacherId, 2l);
            }
        } else {//助教类型
            teacherRepository.saveRolesById(teacherId, 3l);
        }
        return true;
    }

    public List<OptionVo> getDataPermissionsById(Long id) {
        List<OptionVo> dataPermissionVos = Lists.newArrayList();

        for (ExamType examType : ExamType.values()) {
            dataPermissionVos.add(new OptionVo(Long.parseLong(examType.getId() + ""), examType.getText(), false));
        }
        dataPermissionVos.remove(dataPermissionVos.size()-1);//删除全部类型
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
    public int updateStatusByPids(List<Long> pids, Integer status) {
        return teacherRepository.updateStatusByPids(pids, status);
    }

    @Override
    public List<Teacher> findByIdIn(List<Long> ids) {
        return teacherRepository.findByIdIn(ids);
    }

    @Override
    public List<Teacher> findRankTeachers(List<Boolean> isPartTimes, List<TeacherType> types, List<ExamType> examTypes, Long subjectId, List<Long> subjectIds) {
        Specification<Teacher> querySpecific = (root, criteriaQuery, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (isPartTimes != null&&!isPartTimes.isEmpty()) {
                predicates.add(root.get("isPartTime").in(isPartTimes));
            }
//            if (isPartTimes != null) {
//                predicates.add(criteriaBuilder.equal(root.get("isPartTime"), isPartTimes));
//            }
            if (types != null&&!types.isEmpty()) {
                predicates.add(root.get("teacherType").in(types));
            }else{
                predicates.add(criteriaBuilder.or(criteriaBuilder.equal(root.get("teacherType"), TeacherType.JS),
                        criteriaBuilder.equal(root.get("teacherType"), TeacherType.ZJ)));
            }
            if(subjectIds==null||subjectIds.isEmpty()){
                if (examTypes != null&&!examTypes.isEmpty()) {
                    predicates.add(root.get("examType").in(examTypes));
                }
                if (subjectId != null) {
                    predicates.add(criteriaBuilder.equal(root.get("subjectId"), subjectId));
                }
            }else{
                predicates.add(root.get("subjectId").in(subjectIds));
            }
            predicates.add(criteriaBuilder.equal(root.get("status"), TeacherStatus.ZC));

            predicates.add(
                    criteriaBuilder.or(criteriaBuilder.isFalse(root.get("isInvalid")),criteriaBuilder.isNull(root.get("isInvalid")))
                    );

            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
        return teacherRepository.findAll(querySpecific);
    }

    @Override
    public Teacher findByName(String name) {
        return teacherRepository.findOneByName(name);
    }

    @Override
    public Teacher findByNameAndSubjectId(String name, Long subjectId) {
        return teacherRepository.findOneByNameAndTeacherSubjectsSubjectId(name, subjectId);
    }
}
