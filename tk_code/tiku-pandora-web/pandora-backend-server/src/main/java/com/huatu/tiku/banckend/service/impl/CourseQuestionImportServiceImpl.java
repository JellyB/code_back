package com.huatu.tiku.banckend.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.huatu.tiku.banckend.service.CourseBreakpointQuestionService;
import com.huatu.tiku.banckend.service.CourseBreakpointService;
import com.huatu.tiku.banckend.service.CourseExercisesService;
import com.huatu.tiku.banckend.service.CourseKnowledgeService;
import com.huatu.tiku.banckend.service.CourseQuestionImportService;
import com.huatu.tiku.common.CourseQuestionTypeEnum;
import com.huatu.tiku.constant.RedisKeyConstant;
import com.huatu.tiku.dto.request.BatchKnowledgeByCourseReqVO;
import com.huatu.tiku.dto.request.CourseCheckVO;
import com.huatu.tiku.dto.request.CourseQuestionImportVO;
import com.huatu.tiku.dto.request.CourseQuestionVO;
import com.huatu.tiku.entity.CourseBreakpoint;
import com.huatu.tiku.entity.CourseBreakpointQuestion;
import com.huatu.tiku.entity.CourseExercisesQuestion;
import com.huatu.tiku.entity.subject.Subject;
import com.huatu.tiku.enums.SubjectInfoEnum;
import com.huatu.tiku.teacher.service.subject.TeacherSubjectService;
import com.huatu.tiku.util.http.ResponseMsg;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.KnowledgeInfo;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.common.QuestionStatus;
import com.huatu.ztk.question.common.QuestionType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.WeekendSqls;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.huatu.ztk.commons.exception.CommonErrors.INVALID_ARGUMENTS;

@Slf4j
@Service
public class CourseQuestionImportServiceImpl implements CourseQuestionImportService {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private CourseKnowledgeService courseKnowledgeService;
    @Autowired
    private CourseExercisesService courseExercisesService;
    @Autowired
    private CourseBreakpointService courseBreakpointService;
    @Autowired
    private CourseBreakpointQuestionService courseBreakpointQuestionService;
    @Autowired
    private AsyncTaskServiceImpl asyncTaskServiceImpl;
    @Autowired
    private RestTemplate restTemplate;
    @Value("${checkCourseBatchUrl}")
    private String checkCourseBatchUrl;
    @Value("${getRoomCourseListByCourseId}")
    private String getRoomCourseListByCourseId;
    @Autowired
    TeacherSubjectService subjectService;
    @Autowired
    RedisTemplate redisTemplate;


    @Override
    public void batchImport(CourseQuestionImportVO vo, Long userId) throws BizException {

        String path = vo.getPath();
        int type = vo.getType();
        if (type <= 0 || StringUtils.isEmpty(path)) {
            throw new BizException(INVALID_ARGUMENTS);
        }
        //1.处理文件转list
        List<CourseQuestionVO> list = preHandleFile(path, type);
        //2.数据入库
        if (CollectionUtils.isNotEmpty(list)) {
            saveCourseQuestionBatch(list, type, userId);
        }
    }

    @Override
    public String getSubjectByCategory(Long category) {

        List<Subject> subjectList = subjectService.findChildren(category);
        List<Long> subjectIdList = subjectList.stream().filter(i -> i.getParent().equals(category)).filter(i -> i.getLevel().equals(2)).map(Subject::getId).collect(Collectors.toList());

        return StringUtils.join(subjectIdList.toArray(), ",");
    }

    @Override
    public Object getAllSubject() {


        String categorySubjectKey = RedisKeyConstant.CATEGORY_SUBJECT;
        Object allSubject = redisTemplate.opsForValue().get(categorySubjectKey);
        if (null == allSubject) {
            List<Subject> subjectList = subjectService.selectAll();
            Map<Long, String> map = new HashMap<>();
            if (CollectionUtils.isNotEmpty(subjectList)) {
                //考试类型
                List<Subject> categoryList = subjectList.stream().filter(i -> i.getParent().equals(0L)).filter(i -> i.getLevel().equals(1)).collect(Collectors.toList());

                categoryList.forEach(category -> {
                    List<Long> subjectIdList = subjectList.stream().filter(i -> i.getParent().equals(category.getId())).filter(i -> i.getLevel().equals(2)).map(Subject::getId).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(subjectIdList)) {
                        map.put(category.getId(), StringUtils.join(subjectIdList.toArray(), ","));
                    }
                });

                /**
                 *  因为蓝色后台只有教师资格证一个考试类别，但是对应我们这边分为了教师资格证小学和教师资格证两个科目
                 *  为了兼容，所以查询一个考试类别，需要查询中学和小学下的所有的小科目（目前是四个）
                 *
                 */
                Long teacherZhongXue = Long.valueOf(SubjectInfoEnum.TeacherSubjectEnum.teacher_xiaoxue.getKey());
                Long teacherXiaoXue = Long.valueOf(SubjectInfoEnum.TeacherSubjectEnum.teacher_zhongxue.getKey());
                List<String> teacherSubject = new ArrayList<>();
                for (Map.Entry subject : map.entrySet()) {
                    if (subject.getKey().equals(teacherZhongXue) || subject.getKey().equals(teacherXiaoXue)) {
                        List<String> collect = Arrays.stream(subject.getValue().toString().split(",")).collect(Collectors.toList());
                        teacherSubject.addAll(collect);
                    }
                }
                if (CollectionUtils.isNotEmpty(teacherSubject)) {
                    String teacherAllSubjectId = teacherSubject.stream().distinct().collect(Collectors.joining(","));
                    map.put(teacherZhongXue, teacherAllSubjectId);
                    map.put(teacherXiaoXue, teacherAllSubjectId);
                }

                redisTemplate.opsForValue().set(categorySubjectKey, map);
            }
            return map;

        }
        return allSubject;
    }

    /**
     * 课件试题信息同步
     *
     * @param courseIds
     */
    @Override
    public void synchronizeCourse(String courseIds, Long userId) {
        int courseType = CourseQuestionTypeEnum.CourseType.LIVE.getCode();

        ResponseMsg<Map<String, String>> response = restTemplate.getForObject(getRoomCourseListByCourseId + courseIds, ResponseMsg.class);
        Map<String, String> data = response.getData();
        List<Long> courseIdList = Arrays.stream(courseIds.split(","))
                .map(Long::new)
                .collect(Collectors.toList());

        courseIdList.forEach(courseId -> {
            String courseInRoom = data.get(courseId + "");
            //待同步的课件id
            List<Long> roomCourseIdList = Arrays.stream(courseInRoom.split(","))
                    .map(Long::new)
                    .collect(Collectors.toList());

            if (CollectionUtils.isNotEmpty(roomCourseIdList)) {
                roomCourseIdList = roomCourseIdList.stream().filter(i -> !i.equals(courseId)).collect(Collectors.toList());
                //课件绑定的知识点
                List<Long> courseKnowledgeList = courseKnowledgeService.getListByCourseId(courseType, courseId);
                String courseKnowledgeStr = StringUtils.join(courseKnowledgeList.toArray(), ",");

                //课件绑定的题目
                List<CourseBreakpoint> courseBreakpoints = courseBreakpointService.listData(courseType, courseId, "", 0);
                List<CourseBreakpointQuestion> courseBreakpointQuestionList = Lists.newArrayList();

                if (CollectionUtils.isNotEmpty(courseBreakpoints)) {
                    Long courseBreakpointId = courseBreakpoints.get(0).getId();
                    WeekendSqls<CourseBreakpointQuestion> sql = WeekendSqls.custom();
                    sql.andEqualTo(CourseBreakpointQuestion::getBreakpointId, courseBreakpointId);

                    Example example = Example.builder(CourseBreakpointQuestion.class)
                            .andWhere(sql)
                            .orderByAsc("sort")
                            .build();
                    courseBreakpointQuestionList = courseBreakpointQuestionService.selectByExample(example);
                }

                for (Long roomCourseId : roomCourseIdList) {
                    //移除旧的知识点
                    courseKnowledgeService.removeAllByCourseId(courseType, courseId);
                    //更新新的知识点
                    courseKnowledgeService.edit(courseType, courseId, courseKnowledgeStr);

                    List<CourseBreakpoint> roomCourseBreakpoints = courseBreakpointService.listData(courseType, roomCourseId, "", 0);
                    Optional<CourseBreakpoint> any = roomCourseBreakpoints.stream()
                            .filter(courseBreakpoint -> courseBreakpoint.getPosition() == -1)
                            .findAny();
                    CourseBreakpoint roomCourseBreakpoint;
                    if (any.isPresent()) {
                        //移除旧的题目信息
                        roomCourseBreakpoint = any.get();
                        courseBreakpointQuestionService.deleteByBreakpointId(roomCourseBreakpoint.getId());
                    } else {
                        //更新新的题目信息
                        roomCourseBreakpoint = CourseBreakpoint.builder()
                                .courseId(courseId)
                                .courseType(courseType)
                                .pointName("直播随堂练习")
                                .position(-1)
                                .sort(1)
                                .creatorId(userId)
                                .build();

                        courseBreakpointService.save(roomCourseBreakpoint);
                    }
                    Long roomCourseBreakpointId = roomCourseBreakpoint.getId();
                    for (CourseBreakpointQuestion courseBreakpointQuestion : courseBreakpointQuestionList) {
                        courseBreakpointQuestion.setId(null);
                        courseBreakpointQuestion.setBreakpointId(roomCourseBreakpointId);
                    }
                    courseBreakpointQuestionService.insertAll(courseBreakpointQuestionList);
                }

            }


        });


    }

    private void saveCourseQuestionBatch(List<CourseQuestionVO> list, int type, Long userId) {

        int courseType = getCourseType(type);
        //所有的有效课件id
        List<Long> courseIds = new ArrayList<Long>(new HashSet(list.stream().map(vo -> vo.getCourseId()).collect(Collectors.toList())));
        Map<Long, CourseCheckVO> courseInfoMap = checkCourseInfo(courseIds, courseType);
        courseIds = new ArrayList<>(courseInfoMap.keySet());

        //1.按照课件将数据分组
        Map<Long, List<CourseQuestionVO>> courseQuestionMap = getCourseQuestionMap(list, courseIds);

        //2.所有的题目对应的知识点(可优化成分页查询)
        List<Long> questionIds = new ArrayList<Long>(new HashSet(list.stream().map(vo -> vo.getQuestionId()).collect(Collectors.toList())));
        ArrayList<Integer> questionTypeList = new ArrayList<>();
        questionTypeList.add(QuestionType.SINGLE_CHOICE);
        questionTypeList.add(QuestionType.MULTIPLE_CHOICE);
        questionTypeList.add(QuestionType.SINGLE_OR_MULTIPLE_CHOICE);
        questionTypeList.add(QuestionType.WRONG_RIGHT);
        Criteria criteria = Criteria.where("id").in(questionIds)
                .and("type").in(questionTypeList)
                .and("status").is(QuestionStatus.AUDIT_SUCCESS);
        List<Question> questionList = mongoTemplate.find(new Query(criteria), Question.class, "ztk_question_new");
        Map<Integer, List<Long>> questionKnowledgeMap = new HashMap<>();
        Map<Integer, Integer> questionSubjectMap = new HashMap<>();

        for (Question question : questionList) {
            questionKnowledgeMap.put(question.getId(), new ArrayList<>(getQuestionPoints(question)));
            questionSubjectMap.put(question.getId(), question.getSubject());
        }

        //3.过滤科目异常的题目
        for (Long courseInfoId : courseIds) {
            CourseCheckVO courseInfo = courseInfoMap.get(courseInfoId);
            long category = courseInfo.getSubject();
            List<Subject> subjectList = subjectService.findChildren(category);
            List<Integer> subjectIdList = new ArrayList<>();
            subjectList.forEach(subject -> subjectIdList.add(subject.getId().intValue()));

            List<CourseQuestionVO> courseQuestionVOS = courseQuestionMap.get(courseInfoId);
            if (CollectionUtils.isNotEmpty(courseQuestionVOS)) {
                List<CourseQuestionVO> collect = courseQuestionVOS.stream().filter(i -> (null != questionSubjectMap.get(i.getQuestionId().intValue())
                        && subjectIdList.contains(questionSubjectMap.get(i.getQuestionId().intValue())))).collect(Collectors.toList());

                if (CollectionUtils.isNotEmpty(collect)) {
                    courseQuestionMap.put(courseInfoId, collect);
                } else {
                    courseQuestionMap.remove(courseInfoId);
                }
            }
        }
        courseIds = new ArrayList<>(courseQuestionMap.keySet());

        //4.所有课件对应的知识点(可优化成分页查询)
        List<BatchKnowledgeByCourseReqVO> batchKnowledgeReqList = new LinkedList<>();
        for (Long courseId : courseIds) {
            batchKnowledgeReqList.add(BatchKnowledgeByCourseReqVO.builder()
                    .courseId(courseId)
                    .courseType(courseType)
                    .build());
        }
        List<BatchKnowledgeByCourseReqVO> courseKnowledgeList = courseKnowledgeService.getBatchListByCourseId(batchKnowledgeReqList);
        Map<Long, List<Long>> courseKnowledgeMap = courseKnowledgeList.parallelStream()
                .collect(Collectors.toMap(BatchKnowledgeByCourseReqVO::getCourseId, vo -> vo.getPoints()));

        //4.按课件分组，关联待关联知识点（需要把试题所有的知识点都关联到课件上？？产品已确认）
        for (Long courseInfoId : courseIds) {
            List<CourseQuestionVO> courseQuestionVOS = courseQuestionMap.get(courseInfoId);

            //a.获取所有题目对应的知识点
            Set<Long> allQuestionKnowledgeList = new HashSet<>();
            courseQuestionVOS.forEach(courseQuestionVO -> {
                if (null != courseQuestionVO.getQuestionId()) {
                    List<Long> questionKnowledgeList = questionKnowledgeMap.get(courseQuestionVO.getQuestionId().intValue());
                    if (CollectionUtils.isNotEmpty(questionKnowledgeList)) {
                        allQuestionKnowledgeList.addAll(questionKnowledgeList);
                    }
                }
            });

            //b.对比挑选除课件未关联的知识点
            List<Long> courseKnowledge = courseKnowledgeMap.get(courseInfoId);
            Collection subtractKnowledgeList = CollectionUtils.subtract(allQuestionKnowledgeList, courseKnowledge);

            if (CollectionUtils.isNotEmpty(subtractKnowledgeList)) {
                String courseKnowledgeStr = StringUtils.join(subtractKnowledgeList.toArray(), ",");
                //c.课件关联知识点
                courseKnowledgeService.edit(courseType, courseInfoId, courseKnowledgeStr);
            }
        }


        //课后练习
        if (CourseQuestionTypeEnum.CourseQuestionType.LIVE_COURSE_EXERCISE.getCode() == type
                || CourseQuestionTypeEnum.CourseQuestionType.RECORD_COURSE_EXERCISE.getCode() == type) {
            //课件关联课后练习
            for (Map.Entry<Long, List<CourseQuestionVO>> entry : courseQuestionMap.entrySet()) {
                List<CourseQuestionVO> courseQuestionList = entry.getValue();
                Long courseId = entry.getKey();
                LinkedList<CourseExercisesQuestion> courseExercisesQuestions = new LinkedList<>();

                WeekendSqls<CourseExercisesQuestion> sql = WeekendSqls.custom();
                sql.andEqualTo(CourseExercisesQuestion::getCourseId, courseId);
                sql.andEqualTo(CourseExercisesQuestion::getCourseType, courseType);

                Example example = Example.builder(CourseExercisesQuestion.class)
                        .where(sql)
                        .orderByAsc("sort")
                        .build();
                List<CourseExercisesQuestion> existQuestionList = courseExercisesService.selectByExample(example);

                for (int i = 0; i < courseQuestionList.size(); i++) {
                    Long questionId = courseQuestionList.get(i).getQuestionId();
                    //题目信息正确且未关联过，才插入课程题目关联表
                    if (CollectionUtils.isNotEmpty(questionKnowledgeMap.get(questionId.intValue()))) {
                        Optional<CourseExercisesQuestion> anyExistQuestion = existQuestionList.stream()
                                .filter(existQuestion -> existQuestion.getQuestionId().equals(questionId))
                                .findAny();

                        if (!anyExistQuestion.isPresent()) {
                            courseExercisesQuestions.add(CourseExercisesQuestion.builder()
                                    .courseId(courseId)
                                    .courseType(courseType)
                                    .questionId(questionId)
                                    .sort(i + 1)
                                    .userId(userId)
                                    .creatorId(userId)
                                    .build());
                        }

                    }
                }
                courseExercisesService.insertAll(courseExercisesQuestions);
            }

            //随堂练习
        } else if (CourseQuestionTypeEnum.CourseQuestionType.RECORD_BREAK_POINT_QUESTION.getCode() == type
                || CourseQuestionTypeEnum.CourseQuestionType.LIVE_BREAK_POINT_QUESTION.getCode() == type) {
            for (Map.Entry<Long, List<CourseQuestionVO>> entry : courseQuestionMap.entrySet()) {
                List<CourseQuestionVO> courseQuestionList = entry.getValue();
                Long courseId = entry.getKey();


                //录播按照视频位置排序
                if (CourseQuestionTypeEnum.CourseQuestionType.RECORD_BREAK_POINT_QUESTION.getCode() == type) {
                    courseQuestionList = courseQuestionList.stream().sorted(Comparator.comparing(CourseQuestionVO::getPosition)).collect(Collectors.toList());
                } else {
                    //直播按照ppt页数升序
                    courseQuestionList = courseQuestionList.stream().sorted(Comparator.comparing(CourseQuestionVO::getPptIndex)).collect(Collectors.toList());

                }
                List<CourseBreakpoint> courseBreakpoints = courseBreakpointService.listData(courseType, courseId, "", 0);

                List<Long> breakPointIdList = courseBreakpoints.stream().map(vo -> vo.getId()).collect(Collectors.toList());
                List<CourseBreakpointQuestion> existQuestionList = new ArrayList<>();
                WeekendSqls<CourseBreakpointQuestion> sql = WeekendSqls.custom();
                if (CollectionUtils.isNotEmpty(breakPointIdList)) {
                    sql.andIn(CourseBreakpointQuestion::getBreakpointId, breakPointIdList);
                    Example example = Example.builder(CourseBreakpointQuestion.class)
                            .andWhere(sql)
                            .orderByAsc("sort")
                            .build();
                    existQuestionList = courseBreakpointQuestionService.selectByExample(example);
                }


                for (int i = 0; i < courseQuestionList.size(); i++) {
                    CourseQuestionVO courseQuestionVO = courseQuestionList.get(i);
                    int position = courseQuestionVO.getPosition();
                    int pptIndex = courseQuestionVO.getPptIndex();
                    Long questionId = courseQuestionVO.getQuestionId();
                    Optional<CourseBreakpointQuestion> anyExistQuestion = existQuestionList.stream()
                            .filter(existQuestion -> questionId.equals(existQuestion.getQuestionId()))
                            .findAny();

                    if (!anyExistQuestion.isPresent()) {
                        //根据position判断是否有当前节点
                        CourseBreakpoint breakpoint = null;
                        if (CollectionUtils.isNotEmpty(courseBreakpoints)) {
                            Optional<CourseBreakpoint> any = courseBreakpoints.stream()
                                    .filter(courseBreakpoint -> courseBreakpoint.getPosition() == (CourseQuestionTypeEnum.CourseQuestionType.RECORD_BREAK_POINT_QUESTION.getCode() == type ? position : -1))
                                    .findAny();
                            if (any.isPresent()) {
                                breakpoint = any.get();
                            }

                        }

                        //不存在节点，创建节点，获取节点ID，在插入题目
                        if (CollectionUtils.isEmpty(courseBreakpoints) || null == breakpoint) {
                            long count = courseBreakpoints.stream()
                                    .filter(courseBreakpoint -> courseBreakpoint.getPosition() < (CourseQuestionTypeEnum.CourseQuestionType.RECORD_BREAK_POINT_QUESTION.getCode() == type ? position : -1))
                                    .count();

                            breakpoint = CourseBreakpoint.builder()
                                    .courseId(courseId)
                                    .courseType(courseType)
                                    .pointName((CourseQuestionTypeEnum.CourseQuestionType.RECORD_BREAK_POINT_QUESTION.getCode() == type ? "录播随堂练习" : "直播随堂练习") + (int) (count + 1))
                                    .position((CourseQuestionTypeEnum.CourseQuestionType.RECORD_BREAK_POINT_QUESTION.getCode() == type ? position : -1))
                                    .sort((int) count + 1)
                                    .creatorId(userId)
                                    .build();

                            courseBreakpointService.save(breakpoint);
                            courseBreakpoints.add(breakpoint);
                        }
                        Long questionCount = breakpoint.getQuestionCount() == null ? 0 : breakpoint.getQuestionCount();
                        CourseBreakpointQuestion courseBreakpointQuestion = CourseBreakpointQuestion.builder()
                                .breakpointId(breakpoint.getId())
                                .displayStem(1)
                                .pptIndex(CourseQuestionTypeEnum.CourseQuestionType.RECORD_BREAK_POINT_QUESTION.getCode() == type ? -1 : pptIndex)
                                .questionId(courseQuestionVO.getQuestionId())
                                .sort(questionCount.intValue() + 1)
                                .creatorId(userId)
                                .userId(userId)
                                .build();
                        if (null == courseBreakpointQuestion.getPptIndex() || 0 <= courseBreakpointQuestion.getPptIndex()) {
                            courseBreakpointQuestion.setPptIndex(9999);
                        }
                        breakpoint.setQuestionCount(questionCount + 1);
                        courseBreakpointQuestionService.save(courseBreakpointQuestion);
                    }
                }
            }
        }

        //异步，批量更新php端题目数量
        asyncTaskServiceImpl.upQuestionNumOfCourseBatch(courseInfoMap);
    }

    private Map<Long, List<CourseQuestionVO>> getCourseQuestionMap(List<CourseQuestionVO> list, List<Long> courseIds) {
        Map<Long, List<CourseQuestionVO>> courseQuestionMap = new HashMap<>();
        list.forEach(vo -> {
            //能查到课程信息的才填充
            if (courseIds.contains(vo.getCourseId())) {
                List<CourseQuestionVO> courseQuestionVOS = courseQuestionMap.get(vo.getCourseId());
                if (CollectionUtils.isEmpty(courseQuestionVOS)) {
                    courseQuestionVOS = new ArrayList<>();
                }
                Optional<CourseQuestionVO> any = courseQuestionVOS.stream()
                        .filter(existQuestion -> vo.getQuestionId().equals(existQuestion.getQuestionId()))
                        .findAny();
                if (!any.isPresent()) {
                    courseQuestionVOS.add(vo);
                }
                courseQuestionMap.put(vo.getCourseId(), courseQuestionVOS);
            }
        });
        return courseQuestionMap;
    }

    /**
     * 校验课程信息
     */
    public Map checkCourseInfo(List<Long> courseIds, Integer courseType) {

        //校验课件信息
        StringBuilder courseIdStr = new StringBuilder();
        for (Long courseId : courseIds) {
            if (courseIdStr.length() > 0) {
                courseIdStr.append(",");
            }
            courseIdStr.append(courseId);
        }
        String url = "";
        if (courseType == CourseQuestionTypeEnum.CourseType.RECORD.getCode()) {
            url = "lessonId=" + courseIdStr;
        } else if (courseType == CourseQuestionTypeEnum.CourseType.LIVE.getCode()) {
            url = "liveId=" + courseIdStr;
        }
        ResponseMsg<List<LinkedHashMap<String, Object>>> courseData = restTemplate.getForObject(checkCourseBatchUrl + url,
                ResponseMsg.class);
        List<LinkedHashMap<String, Object>> courseList = courseData.getData();
        HashMap<Long, CourseCheckVO> courseMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(courseList)) {
            courseList.forEach(map -> {

                if (null != map.get("type")) {
                    int phpType = Integer.parseInt(map.get("type").toString());
                    int javaCode = CourseQuestionTypeEnum.CourseTypeConvert.getCodeByPHPCode(phpType);
                    courseMap.put(Long.parseLong(map.get("id").toString()),
                            CourseCheckVO.builder().id(Long.parseLong(map.get("id").toString()))
                                    .timeLength(Integer.parseInt(map.get("timeLength").toString()))
                                    .subject(Integer.parseInt(map.get("subjectId").toString()))
                                    .type(javaCode)
                                    .build());

                }
            });
        }
        return courseMap;
    }

    /**
     * 获取试题三级知识点
     */
    private Set<Long> getQuestionPoints(Question question) {
        List<KnowledgeInfo> pointList = question.getPointList();
        Set<Long> questionPoints = new HashSet<>();
        if (CollectionUtils.isNotEmpty(pointList)) {
            try{
                questionPoints = pointList.stream().map(vo -> vo.getPoints().get(2).longValue()).collect(Collectors.toSet());
            }catch (Exception e){
                log.error("获取三级知识点异常:{}", JSONObject.toJSONString(question));
            }
        }

        if (question instanceof GenericQuestion) {
            Long questionPoint = ((GenericQuestion) question).getPoints().get(2).longValue();
            questionPoints.add(questionPoint);
        }

        return questionPoints;
    }


    /**
     * 根据课件id获取课程类型
     *
     * @return
     */
    private int getCourseType(int type) {
        int courseType = -1;
        //课后练习 || 录播随堂练习
        if (CourseQuestionTypeEnum.CourseQuestionType.RECORD_BREAK_POINT_QUESTION.getCode() == type
                || CourseQuestionTypeEnum.CourseQuestionType.RECORD_COURSE_EXERCISE.getCode() == type) {
            courseType = CourseQuestionTypeEnum.CourseType.RECORD.getCode();
            //直播随堂练习
        } else if (CourseQuestionTypeEnum.CourseQuestionType.LIVE_BREAK_POINT_QUESTION.getCode() == type
                || CourseQuestionTypeEnum.CourseQuestionType.LIVE_COURSE_EXERCISE.getCode() == type) {
            courseType = CourseQuestionTypeEnum.CourseType.LIVE.getCode();
        }
        return courseType;

    }


    /**
     * 读取excel文件转换成List
     *
     * @param path
     * @param type
     * @return
     */
    public List<CourseQuestionVO> preHandleFile(String path, int type) {
        List<CourseQuestionVO> list = new LinkedList<>();
        try {
            //创建Excel工作薄
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //设置超时间为3秒
            conn.setConnectTimeout(3 * 1000);
            //防止屏蔽程序抓取而返回403错误
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            //得到输入流
            InputStream inputStream = conn.getInputStream();
            Workbook work;
            work = WorkbookFactory.create(inputStream);

            if (null == work) {
                throw new Exception("创建Excel工作薄为空！");
            }
            Sheet sheet = null;
            Row row = null;
            Cell cell = null;

            for (int i = 0; i < work.getNumberOfSheets(); i++) {

                sheet = work.getSheetAt(i);
                if (sheet == null) {
                    continue;
                }

                for (int j = sheet.getFirstRowNum(); j <= sheet.getLastRowNum(); j++) {
                    row = sheet.getRow(j);
                    if (row == null || row.getFirstCellNum() == j) {
                        continue;
                    }

                    CourseQuestionVO courseQuestionVO = new CourseQuestionVO();
                    //课件id
                    cell = row.getCell(0);
                    String cellValue = getCellValue(cell);
                    if (StringUtils.isNotEmpty(cellValue)) {
                        courseQuestionVO.setCourseId(Long.parseLong(cellValue));
                    }

                    cell = row.getCell(1);
                    String cellValue1 = getCellValue(cell);
                    //题目id
                    if (StringUtils.isNotEmpty(cellValue1)) {
                        if (type == CourseQuestionTypeEnum.CourseQuestionType.RECORD_COURSE_EXERCISE.getCode()
                                || type == CourseQuestionTypeEnum.CourseQuestionType.LIVE_COURSE_EXERCISE.getCode()) {
                            courseQuestionVO.setQuestionId(Long.parseLong(cellValue1));
                            //视频位置
                        } else if (type == CourseQuestionTypeEnum.CourseQuestionType.RECORD_BREAK_POINT_QUESTION.getCode()) {
                            int position = Integer.parseInt(cellValue1);
                            courseQuestionVO.setPosition(position);
                            //ppt页数
                        } else if (type == CourseQuestionTypeEnum.CourseQuestionType.LIVE_BREAK_POINT_QUESTION.getCode()) {
                            courseQuestionVO.setPptIndex(Integer.parseInt(cellValue1));
                        }
                    }

                    cell = row.getCell(2);
                    String cellValue2 = getCellValue(cell);
                    //题目id
                    if (StringUtils.isNotEmpty(cellValue2)) {
                        courseQuestionVO.setQuestionId(Long.parseLong(cellValue2));
                    }

                    if (null != courseQuestionVO && null != courseQuestionVO.getCourseId() && null != courseQuestionVO.getQuestionId()) {
                        list.add(courseQuestionVO);
                    }
                }

            }
            work.close();
            return list;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }


    /**
     * 获取单个单元格的值
     *
     * @param cell
     * @return
     */
    public static String getCellValue(Cell cell) throws BizException {

        String cellValue = "";
        if (cell == null) {
            return "";
        } else {
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_STRING:
                    cellValue = cell.getStringCellValue();
                    break;
                case Cell.CELL_TYPE_BOOLEAN:
                    Boolean val1 = cell.getBooleanCellValue();
                    cellValue = val1.toString();
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                        Date theDate = cell.getDateCellValue();
                        SimpleDateFormat dff = new SimpleDateFormat("HH:mm:ss");
                        cellValue = (theDate.getHours() * 3600 + theDate.getMinutes() * 60 + theDate.getSeconds()) + "";
                    } else {
                        DecimalFormat df = new DecimalFormat("0");
                        cellValue = df.format(cell.getNumericCellValue());
                    }

                    break;
                case Cell.CELL_TYPE_BLANK:
                    break;
                default:
                    throw new BizException(ErrorResult.create(1000580, "文档数据格式错误,请校正后再尝试导入"));
            }

        }
        return cellValue;
    }
}