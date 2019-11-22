package com.huatu.tiku.schedule.biz.service.imple;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.huatu.tiku.schedule.base.config.CustomUser;
import com.huatu.tiku.schedule.base.exception.BadRequestException;
import com.huatu.tiku.schedule.biz.domain.*;
import com.huatu.tiku.schedule.biz.enums.*;
import com.huatu.tiku.schedule.biz.repository.ClassHourInfoRepository;
import com.huatu.tiku.schedule.biz.repository.VideoFeedbackInfoRepository;
import com.huatu.tiku.schedule.biz.service.*;
import com.huatu.tiku.schedule.biz.util.DateformatUtil;
import com.huatu.tiku.schedule.biz.util.ExportExcelUtil;
import com.huatu.tiku.schedule.biz.util.TimeUtil;
import com.huatu.tiku.schedule.biz.util.TimeformatUtil;
import com.huatu.tiku.schedule.biz.vo.Statistics.FeedbackInfoVo;
import com.huatu.tiku.schedule.biz.vo.Statistics.HourInfoVo;
import com.huatu.tiku.schedule.biz.vo.Statistics.StatisticsBodyVo;
import com.huatu.tiku.schedule.biz.vo.Statistics.StatisticsVo;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wangjian
 **/
@Service
public class StatisticsServiceImpl implements StatisticsService {


    private ClassHourInfoRepository classHourInfoRepository;

    private VideoFeedbackInfoRepository videoFeedbackInfoRepository;

    private CourseLiveService courseLiveService;

    private RuleService ruleService;

    private TeacherService teacherService;

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

    public StatisticsServiceImpl(ClassHourInfoRepository classHourInfoRepository, VideoFeedbackInfoRepository videoFeedbackInfoRepository, CourseLiveService courseLiveService, RuleService ruleService, TeacherService teacherService) {
        this.videoFeedbackInfoRepository = videoFeedbackInfoRepository;
        this.classHourInfoRepository = classHourInfoRepository;
        this.courseLiveService = courseLiveService;
        this.ruleService = ruleService;
        this.teacherService = teacherService;
    }

    @Override
    public StatisticsBodyVo getStatistics(Date dateBegin, Date dateEnd, TeacherType teacherType, Long teacherId,List<String> parameters, Boolean exportClassHour) {
        if(parameters==null||parameters.isEmpty()){
            parameters= Lists.newArrayList("1","2","3","4","5","6","7","8","9","10","11","12","13","14","15");
//            parameters.add("1");// 直播授课
//            parameters.add("2");// 录播授课
//            parameters.add("3");// 线下授课
//            parameters.add("4");// 线下分校授课
//            parameters.add("5");// 线上线下练习
//            parameters.add("6");// 线上助教
//            parameters.add("7");// 线下助教
//            parameters.add("8");// 模拟
//            parameters.add("9");// 真题
//            parameters.add("10");// 文章
//            parameters.add("11");// 音频
//            parameters.add("12");// 双师授课
//            parameters.add("13");// 地面讲座
//            parameters.add("14");// 双师练习
//            parameters.add("15");// 地面练习
        }
        StatisticsBodyVo bodyVo = new StatisticsBodyVo();
        List<CourseLive> list = Lists.newArrayList();
        List<CourseLive> lives = courseLiveService.findByDateAndTeacherId(dateBegin, dateEnd, teacherId);
        list.addAll(lives);
        List<ClassHourInfo> hourInfos = classHourInfoRepository.findByDateAndTeacherId(dateBegin, dateEnd, teacherId);
        list.addAll(hourInfos.stream().map(HourInfoVo::new).collect(Collectors.toList()));
        if(parameters.contains("2")){
            List<VideoFeedbackInfo> feedbackInfos = videoFeedbackInfoRepository.findByDateAndTeacherId(dateBegin, dateEnd, teacherId);
            list.addAll(feedbackInfos.stream().map(FeedbackInfoVo::new).collect(Collectors.toList()));
        }
        list.sort(Comparator.comparing(CourseLive::getDate));

        List<Object> body = new ArrayList<>();
        Boolean countFlag = true;//是否有未匹配项
        if (!list.isEmpty()) {
            List<CourseLive> arrList = Lists.newArrayList(list);//arraylist接收
            Collections.reverse(arrList);//逆序 方便处理数据
            String flag = arrList.get(0).getDate().toString();//最后一天日期
            BigDecimal count = BigDecimal.valueOf(0);//总课时
            BigDecimal countDay = BigDecimal.valueOf(0);//每天总课时
            BigDecimal countLiveSK = BigDecimal.valueOf(0);//直播授课
            BigDecimal countLiveLX = BigDecimal.valueOf(0);//直播练习
            BigDecimal countLiveZJ = BigDecimal.valueOf(0);//直播助教
            BigDecimal countXXKSK = BigDecimal.valueOf(0);//线下课授课
            BigDecimal countXXKSchoolSK = BigDecimal.valueOf(0);//线下课分校授课
            BigDecimal countXXKLX = BigDecimal.valueOf(0);//线下课练习
            BigDecimal countXXKZJ = BigDecimal.valueOf(0);//线下课助教
            BigDecimal countReally = BigDecimal.valueOf(0);//真题课时
            BigDecimal countSimulation = BigDecimal.valueOf(0);//模拟题课时
            BigDecimal countVideo = BigDecimal.valueOf(0);//录播课时
            BigDecimal countArticle = BigDecimal.valueOf(0);//录播课时
            BigDecimal countAudio = BigDecimal.valueOf(0);//录播课时
            BigDecimal countOnline = BigDecimal.valueOf(0);//线上助教课时
            BigDecimal countOffline = BigDecimal.valueOf(0);//线下助教课时
            BigDecimal countSSKSK = BigDecimal.valueOf(0);//双师课授课
            BigDecimal countDMJZSK = BigDecimal.valueOf(0);//地面讲座授课
            BigDecimal countSSKLX = BigDecimal.valueOf(0);//双师课练习
            BigDecimal countSSKZJ = BigDecimal.valueOf(0);//双师课助教
            BigDecimal countDMJZLX = BigDecimal.valueOf(0);//地面讲座练习
            for (CourseLive live : arrList) {
                if (live.getSourceId() == null) {  //非滚动直播
                    if (!live.getDate().toString().equals(flag)) {
                        body.add(ImmutableMap.of("countDay", flag + "  " + getCountString(countDay) + "小时"));
                        count = count.add(countDay);
                        countDay = BigDecimal.valueOf(0);
                        flag = live.getDate().toString();
                    }
                    if (live instanceof HourInfoVo) {
                        if (!exportClassHour) {
                            continue;
                        }

                        HourInfoVo vo = (HourInfoVo) live;
                        StatisticsVo statisticsVo = new StatisticsVo();
                        statisticsVo.setType(1);//教研
                        statisticsVo.setDate(vo.getDate().toString());
                        if(parameters.contains("8")) {
                            statisticsVo.setSimulationExam(vo.getSimulationExam());
                            BigDecimal simulationHour = BigDecimal.valueOf(vo.getSimulationHour());
                            statisticsVo.setSimulationHour(simulationHour);//模拟题
                            countSimulation = countSimulation.add(simulationHour);
                            countDay = countDay.add(simulationHour);
                        }
                        if(parameters.contains("9")) {
                            statisticsVo.setReallyExam(vo.getReallyExam());
                            BigDecimal reallyHour = BigDecimal.valueOf(vo.getReallyHour());
                            statisticsVo.setReallyHour(reallyHour);//真题
                            countReally = countReally.add(reallyHour);
                            countDay = countDay.add(reallyHour);
                        }
                        if(parameters.contains("10")) {
                            BigDecimal articleHour = BigDecimal.valueOf(vo.getArticleHour());
                            statisticsVo.setArticleHour(articleHour);//文章
                            countArticle = countArticle.add(articleHour);
                            countDay = countDay.add(articleHour);
                        }
                        if(parameters.contains("11")) {
                            BigDecimal audioHour = BigDecimal.valueOf(vo.getAudioHour());
                            statisticsVo.setAudioHour(audioHour);//音频
                            countAudio = countAudio.add(audioHour);
                            countDay = countDay.add(audioHour);
                        }

                        statisticsVo.setCoefficient("1.0");
                        statisticsVo.setCategoryName("教研");
                        body.add(statisticsVo);
                        continue;
                    }
                    if (live instanceof FeedbackInfoVo) {
                        FeedbackInfoVo vo = (FeedbackInfoVo) live;
                        StatisticsVo statisticsVo = new StatisticsVo();
                        statisticsVo.setType(2);//录播
                        String dateString = vo.getDate().toString();
                        statisticsVo.setDate(vo.getDate().toString());

                        statisticsVo.setCourseName(vo.getCourseName());
                        statisticsVo.setCourseCategory(CourseCategory.VIDEO);
                        statisticsVo.setCategoryName(CourseCategory.VIDEO.getText());
                        Double result = vo.getResult();
                        List<Rule> ruleByData = ruleService.findVideoRuleByData(Integer.valueOf(dateString.replace("-","")));
                        if (ruleByData != null && !ruleByData.isEmpty()) {//有相应的规则
                            Float coefficient = ruleByData.get(0).getCoefficient();//取出相应的规则系数
                            BigDecimal bigDecimal = BigDecimal.valueOf(result).multiply(BigDecimal.valueOf(coefficient));
                            statisticsVo.setVideoHour(getCountString(bigDecimal));//录播课时
                            countVideo = countVideo.add(bigDecimal);//累计录播课时
                            countDay = countDay.add(bigDecimal);//当天
//                        statisticsVo.setCoefficient("1.0");
                            statisticsVo.setCoefficient(String.valueOf(coefficient));//比例系数
                        }else{
                            statisticsVo.setCoefficient("规则未能匹配");//未匹配到规则
                            countFlag = false;
                        }
                        statisticsVo.setCategoryName("录播课");
                        body.add(statisticsVo);
                        continue;
                    }
                    StatisticsVo statisticsVo = new StatisticsVo();
                    statisticsVo.setType(0);
                    statisticsVo.setDate(live.getDate().toString());
                    Integer timeBegin = live.getTimeBegin();//开始时间
                    Integer timeEnd = live.getTimeEnd();//结束时间
                    BigDecimal bigDecimal = TimeUtil.minut2Hour(TimeUtil.interval(timeBegin, timeEnd));//时间差转成小时
                    statisticsVo.setCount(getCountString(bigDecimal));//小时数
                    Float coefficient = null;//默认系数
                    Course course = live.getCourse();
                    CourseCategory courseCategory = course.getCourseCategory();
                    SchoolType schoolType = course.getSchoolType();
                    Boolean onlineFlag=false;//线上助教标记
                    Boolean offlineFlag=false;//线下助教标记

                    // 授课身份
                    TeacherType teacherTypeSK = TeacherType.JS;

                    if (live.getDateInt() <= 20190831) {
                        if (TeacherType.JS.equals(teacherType)) {  //教师类型进行判断
                            for (CourseLiveTeacher clt : live.getCourseLiveTeachers()) {
                                if (teacherId.equals(clt.getTeacherId()) && TeacherType.ZJ.equals(clt.getTeacherType()) &&
                                        !TeacherCourseLevel.ZZZJ.equals(clt.getTeacherCourseLevel())) {  //指定教师在直播当中做助教时 系数为0.3
                                    teacherTypeSK = TeacherType.ZJ;

                                    if (CourseCategory.LIVE.equals(courseCategory)) {
                                        coefficient = 0.30f;
                                        onlineFlag=true;
                                    } else if (CourseCategory.XXK.equals(courseCategory)) {//指定教师在线下课当中做助教时 系数为0.3
                                        coefficient = 0.50f;
                                        offlineFlag=true;
                                    }
                                }
                            }
                        }
                        if (null == coefficient) {
                            List<Rule> ruleByData = ruleService.findRuleByData(live.getDateInt(), live.getCourse().getCourseCategory(), live.getCourse().getExamType(),
                                    live.getCourseLiveCategory(),schoolType);
                            if (ruleByData != null && !ruleByData.isEmpty()) {//有相应的规则
                                coefficient = ruleByData.get(0).getCoefficient();//取出相应的规则系数
                            }
                        }
                    } else {
                        for (CourseLiveTeacher clt : live.getCourseLiveTeachers()) {
                            if (teacherId.equals(clt.getTeacherId())) {
                                teacherTypeSK = clt.getTeacherType();
                                if ((CourseCategory.LIVE.equals(courseCategory) || CourseCategory.SSK.equals(courseCategory)) && teacherTypeSK == TeacherType.ZJ) {
                                    onlineFlag = true;
                                } else if (CourseCategory.XXK.equals(courseCategory) && teacherTypeSK == TeacherType.ZJ) {
                                    offlineFlag = true;
                                }
                            }
                        }

                        List<Rule> ruleByData = ruleService.findRuleByData(live.getDateInt(), live.getCourse().getCourseCategory(), live.getCourse().getExamType(),
                                live.getCourseLiveCategory(),schoolType, teacherTypeSK);
                        if (ruleByData != null && !ruleByData.isEmpty()) {//有相应的规则
                            coefficient = ruleByData.get(0).getCoefficient();//取出相应的规则系数
                        }
                    }

                    statisticsVo.setCourseLiveCategory(live.getCourseLiveCategory());
                    statisticsVo.setCourseCategory(courseCategory);
                    String categoryName;
                    if(null!=schoolType&&CourseCategory.XXK.equals(courseCategory)){
                        categoryName=courseCategory.getText()+"("+schoolType.getText()+")";
                    }else{
                        categoryName=courseCategory.getText();
                    }
                    statisticsVo.setCategoryName(categoryName);
                    if (null != coefficient) {
                        bigDecimal = bigDecimal.multiply(BigDecimal.valueOf(coefficient));//乘法

                        if (teacherTypeSK == TeacherType.ZJ) {
                            if (CourseCategory.LIVE.equals(courseCategory)) {
                                if (parameters.contains("6")) {
                                    countLiveZJ = countLiveZJ.add(bigDecimal);
                                } else {
                                    continue;
                                }
                            } else if (CourseCategory.XXK.equals(courseCategory)) {
                                if (parameters.contains("7")) {
                                    countXXKZJ = countXXKZJ.add(bigDecimal);
                                } else {
                                    continue;
                                }
                            } else if (CourseCategory.SSK.equals(courseCategory)) {
                                if (parameters.contains("6")) {
                                    countSSKZJ = countSSKZJ.add(bigDecimal);
                                } else {
                                    continue;
                                }
                            }
                        } else {
                            if (CourseLiveCategory.SK.equals(live.getCourseLiveCategory())) {  //授课或者空
                                if (CourseCategory.LIVE.equals(courseCategory)) {
                                    if(parameters.contains("1")) {
                                        countLiveSK = countLiveSK.add(bigDecimal);
                                    }else{
                                        continue;
                                    }
                                }else if (CourseCategory.XXK.equals(courseCategory)) {  //线下授课 判断是否分校
                                    if(SchoolType.HTZX.equals(course.getSchoolType())){ //华图在线
                                        if(parameters.contains("3")) {
                                            countXXKSK = countXXKSK.add(bigDecimal);
                                        }else{
                                            continue;
                                        }
                                    } else{ //非华图在线
                                        if(parameters.contains("4")) {
                                            countXXKSchoolSK=countXXKSchoolSK.add(bigDecimal);//累计分校线下课授课
                                        }else{
                                            continue;
                                        }
                                    }
                                } else if (CourseCategory.SSK.equals(courseCategory)) {
                                    if(parameters.contains("12")) {
                                        countSSKSK = countSSKSK.add(bigDecimal);
                                    }else{
                                        continue;
                                    }
                                } else if (CourseCategory.DMJZ.equals(courseCategory)) {
                                    if(parameters.contains("13")) {
                                        countDMJZSK = countDMJZSK.add(bigDecimal);
                                    }else{
                                        continue;
                                    }
                                }
                            }else if (CourseLiveCategory.LX.equals(live.getCourseLiveCategory())) {
                                if (CourseCategory.LIVE.equals(courseCategory)) {
                                    if(parameters.contains("5")) {
                                        countLiveLX = countLiveLX.add(bigDecimal);
                                    }else{
                                        continue;
                                    }
                                }else if (CourseCategory.XXK.equals(courseCategory)) {
                                    if(parameters.contains("5")) {
                                        countXXKLX = countXXKLX.add(bigDecimal);
                                    }else{
                                        continue;
                                    }
                                } else if (CourseCategory.SSK.equals(courseCategory)) {
                                    if(parameters.contains("14")) {
                                        countSSKLX = countSSKSK.add(countSSKLX);
                                    }else{
                                        continue;
                                    }
                                } else if (CourseCategory.DMJZ.equals(courseCategory)) {
                                    if(parameters.contains("15")) {
                                        countDMJZLX = countDMJZLX.add(bigDecimal);
                                    }else{
                                        continue;
                                    }
                                }
                            }
                        }
                        if(onlineFlag&&parameters.contains("6")){
                            countOnline=countOnline.add(bigDecimal);
                        }
                        if(offlineFlag&&parameters.contains("7")){
                            countOffline=countOffline.add(bigDecimal);
                        }
                        countDay = countDay.add(bigDecimal);
                        statisticsVo.setBigDecimal(bigDecimal);
                        statisticsVo.setCoefficient(String.valueOf(coefficient));//比例系数

                    } else {
                        statisticsVo.setCoefficient("规则未能匹配");//未匹配到规则
                        countFlag = false;
                    }

                    statisticsVo.setTime(TimeformatUtil.format(timeBegin) + "-" + TimeformatUtil.format(timeEnd));
                    statisticsVo.setLiveName(live.getName());
                    statisticsVo.setCourseName(course.getName());
                    statisticsVo.setCourseLiveCategory(live.getCourseLiveCategory());
                    body.add(statisticsVo);
                }
            }
            body.add(ImmutableMap.of("countDay", flag + " " + getCountString(countDay) + "小时"));
            count = count.add(countDay);
            Collections.reverse(body);//正序
            bodyVo.setCount(countFlag ? getCountString(count) : "?");
            bodyVo.setBody(body);
            bodyVo.setCountLiveSK(getCountString(countLiveSK));
            bodyVo.setCountLiveLX(getCountString(countLiveLX));
            bodyVo.setCountLiveZJ(getCountString(countLiveZJ));
            bodyVo.setCountXXKSK(getCountString(countXXKSK));
            bodyVo.setCountXXKLX(getCountString(countXXKLX));
            bodyVo.setCountXXKZJ(getCountString(countXXKZJ));
            bodyVo.setCountReally(getCountString(countReally));
            bodyVo.setCountSimulation(getCountString(countSimulation));
            bodyVo.setCountVideo(getCountString(countVideo));
            bodyVo.setCountArticle(getCountString(countArticle));
            bodyVo.setCountAudio(getCountString(countAudio));
            bodyVo.setCountXXKSchoolSK(getCountString(countXXKSchoolSK));
            bodyVo.setCountOnline(getCountString(countOnline));
            bodyVo.setCountOffline(getCountString(countOffline));
            bodyVo.setCountSSKSK(getCountString(countSSKSK));
            bodyVo.setCountSSKLX(getCountString(countSSKLX));
            bodyVo.setCountSSKZJ(getCountString(countSSKZJ));
            bodyVo.setCountDMJZSK(getCountString(countDMJZSK));
            bodyVo.setCountDMJZLX(getCountString(countDMJZLX));
        } else {
            bodyVo.setCount("0");
            bodyVo.setBody(null);
            bodyVo.setCountLiveSK("0");
            bodyVo.setCountLiveLX("0");
            bodyVo.setCountLiveZJ("0");
            bodyVo.setCountXXKSK("0");
            bodyVo.setCountXXKLX("0");
            bodyVo.setCountXXKZJ("0");
            bodyVo.setCountReally("0");
            bodyVo.setCountSimulation("0");
            bodyVo.setCountVideo("0");
            bodyVo.setCountArticle("0");
            bodyVo.setCountAudio("0");
            bodyVo.setCountXXKSchoolSK("0");
            bodyVo.setCountOnline("0");
            bodyVo.setCountOffline("0");
            bodyVo.setCountSSKSK("0");
            bodyVo.setCountSSKLX("0");
            bodyVo.setCountSSKZJ("0");
            bodyVo.setCountDMJZSK("0");
            bodyVo.setCountDMJZLX("0");
        }
        return bodyVo;
    }

    @Override
    public List<StatisticsBodyVo> getRankStatisticsBodyVos(List<String> parameters,
                                                           List<Boolean> isPartTimes,
                                                           List<TeacherType> types,
                                                           ExamType examType, Long subjectId,
                                                           Date dateBegin,
                                                           Date dateEnd,
                                                           Pageable page,
                                                           CustomUser user, List<Long> subjectIds) {
        if (dateBegin == null) {
            throw new BadRequestException("请设置时间范围");
        }
        if (dateEnd == null) {
            dateEnd = new Date();
        }
        if (dateBegin.after(dateEnd)) {
            throw new BadRequestException("开始时间不能晚于结束时间");
        }
        List<ExamType> examTypes = Lists.newArrayList();
        // 可以查看全部的角色
        List<String> roleNames = Lists.newArrayList("超级管理员", "人力", "教学管理组");
        // 当前用户角色
        Set<Role> roles = user.getRoles();
        // 管理员
        Optional<Role> adminFlag = roles.stream().filter(role -> roleNames.contains(role.getName())).findFirst();
        if (!adminFlag.isPresent()) { //不是指定角色 判断权限
            Optional<Role> jwFlag = roles.stream().filter(role -> role.getName().equals("教务") || role.getName().equals("运营") || role.getName().equals("录播教务") || role.getName().equals("录播产品")).findFirst();
            if (jwFlag.isPresent()) { // 教务
                // 数据权限
                Set<ExamType> dataPermissioins = user.getDataPermissions();
                if (examType == null) {
                    examTypes.addAll(dataPermissioins);
                } else {
                    examTypes.add(examType);
                }
            } else { //不是教务判断是否是组长
                Optional<Role> zzFlag = roles.stream().filter(role -> role.getName().equals("组长")).findFirst();
                Boolean leaderFlag = user.getLeaderFlag();
                if (leaderFlag && zzFlag.isPresent()) { //组长
                    if (examType == null) {
                        examTypes.add(user.getExamType());
                    } else {
                        examTypes.add(examType);
                    }
                    if (subjectId == null) {
                        subjectId = user.getSubjectId();
                    }
                } else { //不是教务 不是组长报错
                    throw new BadRequestException("无讲师课表的查看权限");
                }
            }
        } else {  //指定角色
            if (null != examType) { //指定类型添加类型 不指定类型添加全部
                examTypes.add(examType);
            } else {  //不指定类型添加全部
                examTypes = null;  //设置为null 即可查询全部数据
            }
        }
        List<Teacher> rankTeachers = teacherService.findRankTeachers(isPartTimes, types, examTypes, subjectId,subjectIds);
        List<StatisticsBodyVo> vos = Lists.newArrayList();
        for (Teacher rankTeacher : rankTeachers) {
            StatisticsBodyVo statistics = getStatistics(dateBegin, dateEnd, rankTeacher.getTeacherType(), rankTeacher.getId(),parameters, true);
            statistics.setTeacherId(rankTeacher.getId());
            statistics.setTeacherName(rankTeacher.getName());
            statistics.setBody(null);
            vos.add(statistics);
        }
        Sort.Direction sort = page.getSort().getOrderFor("count").getDirection();
        vos.sort((o1, o2) -> {
            String count = o1.getCount();
            String count2 = o2.getCount();
            if ("?".equals(count)) {
                count = "0";
            }
            if ("?".equals(count2)) {
                count2 = "0";
            }
            if (Sort.Direction.ASC.equals(sort)) {
                return Double.valueOf(count).compareTo(Double.valueOf(count2));
            } else {
                return -Double.valueOf(count).compareTo(Double.valueOf(count2));
            }
        });
        return vos;
    }

    @Override
    public HSSFWorkbook getStatisticsExcel(Date dateBegin, Date dateEnd, Teacher teacher, ExamType examType,Subject subject, Boolean exportClassHour) {
        StatisticsBodyVo map = getStatistics(dateBegin, dateEnd, teacher.getTeacherType(), teacher.getId(),null, exportClassHour);
        List<Object> body = map.getBody();
        if (null == body || body.isEmpty()) {
            return null;
        }
        DecimalFormat df = new DecimalFormat("0.00");
        List<List<String>> rowlist = Lists.newArrayList();
        for (Object object : body) {
            if (object instanceof StatisticsVo) {
                StatisticsVo vo = (StatisticsVo) object;
                List<String> cellLis = Lists.newArrayList();
                cellLis.add(vo.getDate());//日期
                String time = vo.getTime();
                if (StringUtils.isNotBlank(time)) {
                    cellLis.add(time);
                } else {
                    cellLis.add("/");
                }
                String liveName = vo.getLiveName();
                if (StringUtils.isNotBlank(liveName)) {
                    cellLis.add(liveName);
                } else {
                    if (1 == vo.getType()) { //教研
                        cellLis.add("真题数：" + vo.getReallyExam() + ",真题课时：" + vo.getReallyHour() + "\n模拟数：" + vo.getSimulationExam() + ",模拟课时：" + vo.getSimulationHour()
                                + ",教研文章：" + vo.getArticleHour()
                                + ",音频录制：" + vo.getAudioHour());
                    }
                    if (2 == vo.getType()) {//录播
                        cellLis.add("录播课时:" + vo.getVideoHour());
                    }
                }
                cellLis.add(teacher.getName());
                String categoryName = vo.getCategoryName();
                if (StringUtils.isNotBlank(categoryName)) {
                    cellLis.add(vo.getCategoryName());
                } else {
                    cellLis.add("教研课时反馈");
                }
                String courseName = vo.getCourseName();
                if (StringUtils.isNotBlank(courseName)) {
                    cellLis.add(courseName);
                } else {
                    cellLis.add("/");
                }
                String coefficient = vo.getCoefficient();
                if (StringUtils.isNotBlank(coefficient)) {
                    cellLis.add(coefficient);
                } else {
                    cellLis.add("/");
                }
                if (null != vo.getBigDecimal()) {
                    cellLis.add(df.format(vo.getBigDecimal()));
                } else {
                    if (1 == vo.getType()) { //教研
                        cellLis.add(df.format(vo.getReallyHour().add(vo.getSimulationHour()).add(vo.getArticleHour()).add(vo.getAudioHour())));
                    } else if (2 == vo.getType()) {
                        cellLis.add(vo.getVideoHour());
                    } else {
                        cellLis.add("?");
                    }
                }
                rowlist.add(cellLis);
            }
        }
        String count = map.getCount();
        String[] countStrings = new String[16];
        countStrings[0] = map.getCountLiveSK();
        countStrings[1] = map.getCountLiveLX();
        countStrings[2] = new BigDecimal(map.getCountXXKSK()).add(new BigDecimal(map.getCountXXKSchoolSK())).toString();
        countStrings[3] = map.getCountXXKLX();
        countStrings[4] = map.getCountReally();
        countStrings[5] = map.getCountSimulation();
        countStrings[6] = map.getCountVideo();
        countStrings[7] = map.getCountArticle();
        countStrings[8] = map.getCountAudio();
        countStrings[9] = map.getCountSSKSK();
        countStrings[10] = map.getCountSSKLX();
        countStrings[11] = map.getCountDMJZSK();
        countStrings[12] = map.getCountDMJZLX();
        countStrings[13] = map.getCountLiveZJ();
        countStrings[14] = map.getCountXXKZJ();
        countStrings[15] = map.getCountSSKZJ();
        String[] strings = new String[3];
        strings[0] = "总课时数 " + count + " 小时";
        strings[1] = "时间范围: " + DateformatUtil.format0(dateBegin) + "~" + DateformatUtil.format0(dateEnd);
        StringBuilder sb = new StringBuilder();
        sb.append("教师选择: ");
        if (null != examType) {
            sb.append(examType.getText());
            sb.append("-");
        }
        if (null != subject) {
            sb.append(subject.getName());
            sb.append("-");
        }
        sb.append(teacher.getName());
        strings[2] = sb.toString();
        return ExportExcelUtil.getHSSFWorkbook("课时统计明细表",
                strings, rowlist, countStrings, exportClassHour);
    }

    private String getCountString(BigDecimal count){
        if(0==(BigDecimal.ZERO.compareTo(count))){
            return "0";
        }else{
            return DECIMAL_FORMAT.format(count);
        }
    }
}
