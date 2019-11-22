package com.huatu.tiku.schedule.biz.service.imple;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huatu.tiku.schedule.base.exception.BadRequestException;
import com.huatu.tiku.schedule.biz.bean.ImportTeacherCourseBean;
import com.huatu.tiku.schedule.biz.domain.*;
import com.huatu.tiku.schedule.biz.dto.php.PHPUpdateTeacherDto;
import com.huatu.tiku.schedule.biz.enums.*;
import com.huatu.tiku.schedule.biz.repository.*;
import com.huatu.tiku.schedule.biz.service.ImportService;
import com.huatu.tiku.schedule.biz.util.DateformatUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author wangjian
 **/
@Service
@EnableAspectJAutoProxy
public class ImportServiceImpl implements ImportService {

    Logger LOG = LoggerFactory.getLogger(TeacherServiceImpl.class);

    private final TeacherRepository teacherRepository;

    private final CourseRepository courseRepository;

    private final CourseLiveRepository courseLiveRepository;

    private final CourseLiveTeacherRepository courseLiveTeacherRepository;

    private final TeacherSubjectRepository teacherSubjectRepository;

    private final SubjectRepository subjectRepository;

    @Autowired
    public ImportServiceImpl(TeacherRepository teacherRepository, CourseRepository courseRepository, CourseLiveRepository courseLiveRepository, CourseLiveTeacherRepository courseLiveTeacherRepository, TeacherSubjectRepository teacherSubjectRepository, SubjectRepository subjectRepository) {
        this.teacherRepository = teacherRepository;
        this.courseRepository = courseRepository;
        this.courseLiveRepository = courseLiveRepository;
        this.courseLiveTeacherRepository = courseLiveTeacherRepository;
        this.teacherSubjectRepository = teacherSubjectRepository;
        this.subjectRepository = subjectRepository;
    }

    @Override
    @Transactional
    public void importTeacherCourse(List<List<List<String>>> importList, Long courseId, Boolean flag) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        Integer year = Integer.parseInt(sdf.format(new Date()));

        for (List<List<String>> list : importList) {
            if (list.size() > 0) {
                List<ImportTeacherCourseBean> beans = Lists.newArrayList();
                Set<String> teacherNames = Sets.newHashSet();
                List<Date> dateList = Lists.newArrayList();

                for (int i = 0; i < list.size(); i++) {
                    List<String> currentList = list.get(i);
                    if (currentList.get(1).trim().equals("时间")) {
                        dateList.clear();
                        dateList.add(null);
                        dateList.add(null);
                        for (int j = 2; j < currentList.size(); j++) {
                            Date date = DateformatUtil.getDateFromSDF5(year + "年" + currentList.get(j));
                            dateList.add(date);
                        }
                    } else {
                        String[] strings = currentList.get(1).split("\n");

                        String[] times = new String[0];
                        try {
                            times = strings[1].split("-");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        for (int j = 2; j < currentList.size(); j++) {
                            String content = currentList.get(j);
                            if (StringUtils.isNotBlank(content)) {
                                if (flag) {
                                    ImportTeacherCourseBean bean = new ImportTeacherCourseBean();
                                    String[] contents = content.split("\n");
                                    String courseName = contents[0];
                                    String teacherName = contents[1];
                                    bean.setDate(dateList.get(j));
                                    bean.setTimeBegin(Integer.parseInt(times[0].trim().replace(":", "")));
                                    bean.setTimeEnd(Integer.parseInt(times[1].trim().replace(":", "")));
                                    bean.setCourseName(courseName);
                                    bean.setTeacherName(teacherName);
                                    teacherNames.add(teacherName);
                                    beans.add(bean);
                                } else {
                                    ImportTeacherCourseBean bean = new ImportTeacherCourseBean();
                                    String[] contents = content.split("\n");
                                    String courseFullName = contents[0];
                                    String courseName = courseFullName.substring(0, courseFullName.length() - 1);
                                    String teacherName = contents[1];
                                    bean.setDate(dateList.get(j));
                                    bean.setTimeBegin(Integer.parseInt(times[0].trim().replace(":", "")));
                                    bean.setTimeEnd(Integer.parseInt(times[1].trim().replace(":", "")));
                                    bean.setCourseName(courseName);
                                    String st = courseFullName.substring(courseFullName.length() - 1, courseFullName.length());
                                    bean.setTeacherName(teacherName);
                                    teacherNames.add(teacherName);
                                    beans.add(bean);
                                }

                            }
                        }
                    }
                }
                Map<String, Long> teacherMap = validateTeacher(teacherNames);//校验教师的合法性
                Course course = courseRepository.findOne(courseId);//查找课程
                batchAddTeacherCourse(beans, course, teacherMap);
//			((TeacherServiceImpl)AopContext.currentProxy()).batchAddTeacherCourse(beans, courseId, teacherMap);
            }
        }
    }

    /**
     * @description: 导入非滚动排课
     * @author duanxiangchao
     * @date 2018/5/14 下午4:35
     */
    public void batchAddTeacherCourse(List<ImportTeacherCourseBean> beans, Course course, Map<String, Long> teacherMap) {
        CourseLive lastCourseLive = null;
        Long lastLiveId = null;
        for (int i = 0; i < beans.size(); i++) {
            ImportTeacherCourseBean courseBean = beans.get(i);
            if (courseBean != null) {
                String teacherNames = courseBean.getTeacherName();   //取出教师名称
                String[] teachers = teacherNames.split(",");//英文逗号
                if (teachers.length == 1) {    //分割成数组
                    teachers = teacherNames.split("，");//中文逗号
                }
                List<Long> list = new ArrayList();//教师id集合
                Arrays.asList(teachers).forEach(name -> {
                    list.add(teacherMap.get(name));
                });
                if (i > 0) {  //第二条开始 取上条数据
                    ImportTeacherCourseBean lastCourseBean = beans.get(i - 1);
                    if (courseBean.getDate().equals(lastCourseBean.getDate()) && courseBean.getTimeBegin().equals(lastCourseBean.getTimeBegin()) &&
                            courseBean.getTimeEnd().equals(lastCourseBean.getTimeEnd())) {  //日期时间相等
                        courseBean.setCourseLiveId(lastLiveId);
                        //添加教师直播关联
                        for (Long teacherId : list) {    //使用上一条直播id添加数据
                            if (!courseBean.getCourseName().equals(lastCourseBean.getCourseName())) {  //内容不相等 进行拼接
                                String name = lastCourseLive.getName();
                                name = name + "，" + courseBean.getCourseName();
                                lastCourseLive.setName(name);
                                courseLiveRepository.save(lastCourseLive);
                            }
                            CourseLiveTeacher courseLiveTeacher = courseLiveTeacherWrapper(lastLiveId, teacherId,
                                     courseBean.getSubject());
                            courseLiveTeacherRepository.save(courseLiveTeacher);
                        }
                        continue;
                    }
                }
                //添加直播
                lastCourseLive = courseLiveWrapper(courseBean, course.getId(), null);
                courseLiveRepository.save(lastCourseLive);
                lastLiveId = lastCourseLive.getId();
                courseBean.setCourseLiveId(lastLiveId);
                //添加教师直播关联
                for (Long teacherId : list) {
                    CourseLiveTeacher courseLiveTeacher = courseLiveTeacherWrapper(lastLiveId, teacherId,
                             courseBean.getSubject());
                    courseLiveTeacherRepository.save(courseLiveTeacher);
                }
                ;

            }
        }
    }


    /**
     * @description: 导入滚动排课
     * @author duanxiangchao
     * @date 2018/5/14 下午4:34
     */
    public void importCourseRoll(List<List<List<String>>> importList) {
        for (int k = 0; k < 1; k++) {
            //sheet页遍历
            List<List<ImportTeacherCourseBean>> lists = new ArrayList<List<ImportTeacherCourseBean>>();
            lists.add(null);
            lists.add(null);
            List<List<String>> sheetList = importList.get(k);
            Set<String> teacherNames = getTeacherNames(sheetList);
            Map<String, Long> teacherMap = validateTeacher(teacherNames);
            Date lastDate = null;
            for (int i = 1; i < sheetList.size(); i++) {
                //从第二行开始，获取单行数据
                List<String> rowData = sheetList.get(i);
                //获取第一列日期
                String dateStr = rowData.get(0);
                if (StringUtils.isNotBlank(dateStr)) {
                    Date currentDate = null;
                    currentDate = HSSFDateUtil.getJavaDate(Double.parseDouble(rowData.get(0)));
                    int countRow = 1;
                    if (currentDate.equals(lastDate)) {
                        //当天有两节课
                        countRow = 2;
                    }
                    lastDate = currentDate;
                    //获取课程时间
                    WeekEnum weekEnum = WeekEnum.create(rowData.get(1).trim());
                    ScheduleTimeEnum scheduleTimeEnum = ScheduleTimeEnum.getWeekScheduleTimeEnum(weekEnum, countRow);
                    //时间日期获取完毕，开始遍历数据
                    String lastStr = "";
                    for (int j = 2; j < rowData.size(); j++) {
                        String dataStr = rowData.get(j);
                        if (StringUtils.isNotBlank(dataStr)) {
                            //获取列bean集合
                            List<ImportTeacherCourseBean> beans = null;
                            if (lists.size() == j) {
                                //集合没有初始化
                                beans = new ArrayList<ImportTeacherCourseBean>();
                                lists.add(beans);
                            } else if (lists.size() < j) {
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
                            String[] strs = dataStr.split(coursePhaseStr);
                            courseBean.setCourseName(strs[0] + coursePhaseStr);
                            courseBean.setTeacherName(strs[1]);
                            beans.add(courseBean);
                            if (dataStr.equals(lastStr)) {
                                //滚动排课
                                try {
                                    List<ImportTeacherCourseBean> preBeans = lists.get(j - 1);
                                    courseBean.setRoll(true);
                                    courseBean.setTeacherCourseBean(getTeacherCourseBean(preBeans.get(preBeans.size() - 1)));
                                } catch (Exception e) {
                                    LOG.error("错误数据： lastStr:" + lastStr + "dataStr:" + dataStr);
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
            for (int i = 0; i < lists.size(); i++) {
                if (lists.get(i) != null) {
                    Course course = new Course();
                    course.setName("导入" + nowStr + "_" + (count++));
                    Date currentDate = new Date();
                    course.setDateBegin(new Date());
                    course.setAssistantFlag(false);
                    course.setCompereFlag(false);
                    course.setControllerFlag(false);
                    course.setSatFlag(false);
                    course.setSunFlag(false);
                    course.setStatus(CourseStatus.WC);
                    course.setDateEnd(DateUtils.addDays(currentDate, 60));
                    course.setCourseCategory(CourseCategory.LIVE);
                    course.setExamType(ExamType.GWY);
                    courseRepository.save(course);
                    batchAddTeacherCourse(lists.get(i), course, teacherMap);
                }
            }
        }
    }

    public void importCourse(List<List<List<String>>> importList, String courseName, ExamType examType, CourseCategory category) {
        for (int k = 0; k < 1; k++) {
            //sheet页遍历
            List<ImportTeacherCourseBean> beans = new ArrayList<ImportTeacherCourseBean>();
            List<List<String>> sheetList = importList.get(k);
            Set<String> teacherNames = getTeacherNamesDemo(sheetList);
            Map<String, Long> teacherMap = validateTeacher(teacherNames);
            for (int i = 1; i < sheetList.size(); i++) {
                //从第二行开始，获取单行数据
                List<String> rowData = sheetList.get(i);  //单行数据
                //获取第一列日期
                String dateStr = rowData.get(0);
                if (StringUtils.isNotBlank(dateStr)) {
                    if (!StringUtils.isNotBlank(rowData.get(3))) {
                        if (!StringUtils.isNotBlank(rowData.get(5))) {
                            continue;
                        }
                    }

                    Date currentDate = null;
                    currentDate = HSSFDateUtil.getJavaDate(Double.parseDouble(dateStr));
                    //时间日期获取完毕，开始遍历数据
                    String dateString = rowData.get(2);//时间
                    String[] times = dateString.split("-");
                    if (times.length != 2) {
                        times = dateString.split("—");
                    }
                    String beginTime = times[0].replace("：", "");
                    beginTime = beginTime.replace(":", "");
                    String endTime = times[1].replace("：", "");
                    endTime = endTime.replace(":", "");
                    Integer timeBegin = Integer.parseInt(beginTime);
                    Integer timeEnd = Integer.parseInt(endTime);
                    //获取列bean集合
                    ImportTeacherCourseBean courseBean = new ImportTeacherCourseBean();
                    courseBean.setDate(currentDate);
                    courseBean.setTimeBegin(timeBegin);
                    courseBean.setTimeEnd(timeEnd);

                    courseBean.setCourseName(rowData.get(3));//内容
                    courseBean.setSubject(rowData.get(4));//科目
                    String teacherName = rowData.get(5);
                    courseBean.setTeacherName(teacherName.trim());//教师
                    if (rowData.size() >= 8 && StringUtils.isNotBlank(rowData.get(7))) {
                        String liveCategory = rowData.get(7);//阶段字符串
                        if ("授课".equals(liveCategory)) {
                            courseBean.setCourseLiveCategory(CourseLiveCategory.SK);
                        }
                        if ("练习".equals(liveCategory)) {
                            courseBean.setCourseLiveCategory(CourseLiveCategory.LX);
                        }
                    }
                    beans.add(courseBean);
                }
            }

            Course course = new Course();
            course.setName(courseName);
            Date currentDate = new Date();
            course.setDateBegin(new Date());
            course.setAssistantFlag(false);
            course.setCompereFlag(false);
            course.setControllerFlag(false);
            course.setSatFlag(false);
            course.setSunFlag(false);
            course.setStatus(CourseStatus.WC);
            if (null != category) {//默认直播
                course.setCourseCategory(category);
            } else {
                course.setCourseCategory(CourseCategory.LIVE);
            }
            course.setDateEnd(DateUtils.addDays(currentDate, 60));
            course.setExamType(examType);
            courseRepository.save(course);
            batchAddTeacherCourse(beans, course, teacherMap);
        }
    }

    @Override//导入竖版课表滚动排课
    @Transactional
    public void importCourseRoll(List<List<List<String>>> list , ExamType examType, CourseCategory category) {
        for (int k = 0; k < 1; k++) {  //第一个sheet页
            Set<String> teacherNames = new HashSet<>();
            List<List<String>> sheetList = list.get(k);//本页数据
            List<String> strings = sheetList.get(0);//第一行表头
            int size = strings.size() - 3;//总期数
            Map<Integer, List> map = new HashMap();//每期的数据
            List<String> courseNameList=new ArrayList();
            for (int num = 1; num <= size; num++) {
                courseNameList.add(strings.get(num+2));
                map.put(num, new ArrayList());//每期一个空集合
            }
            for (int i = 1; i < sheetList.size(); i++) {
                List<String> rowData = sheetList.get(i);//行数据
                //获取第一列日期
                String dateStr = rowData.get(0);
                if (StringUtils.isNotBlank(dateStr)) {
                    Date currentDate = null;
                    currentDate = HSSFDateUtil.getJavaDate(Double.parseDouble(dateStr));
                    //时间日期获取完毕，开始遍历数据
                    String dateString = rowData.get(2).trim();//时间
                    String[] times = dateString.split("-");
                    if (times.length != 2) {
                        times = dateString.split("—");
                    }
                    String beginTime = times[0].replace("：", "");
                    beginTime = beginTime.replace(":", "");
                    String endTime = times[1].replace("：", "");
                    endTime = endTime.replace(":", "");
                    Integer timeBegin = Integer.parseInt(beginTime);//开始时间
                    Integer timeEnd = Integer.parseInt(endTime);//结束时间
                    for (int j = 3; j < strings.size(); j++) {
                        String stringData = rowData.get(j);//课程数据
                        List lives = map.get(j - 2);
                        if (StringUtils.isNotBlank(stringData)) {  //有内容时
                            ImportTeacherCourseBean courseBean = new ImportTeacherCourseBean();
                            courseBean.setDate(currentDate);//日期
                            courseBean.setTimeBegin(timeBegin);//开始时间
                            courseBean.setTimeEnd(timeEnd);//结束时间
                            String coursePhaseStr = stringData.replaceAll("[^0-9]", "");
                            String[] strs = stringData.split(coursePhaseStr);
                            String courseLiveName=strs[0];
                            courseBean.setCourseName(courseLiveName + coursePhaseStr);
                            courseBean.setTeacherName(strs[1]);
                            teacherNames.add(strs[1]);//添加教师姓名集合进行校验
                            String[] subject = courseLiveName.split(" ");
                            if(null!=subject&&subject.length==2){
                                courseBean.setSubject(subject[1]);
                            }else{
                                courseBean.setSubject(courseLiveName);
                            }
                            lives.add(courseBean);
                        } else {  //无内容
                            lives.add(null);
                        }
                    }
                } else {
                    break;
                }

            }
            Map<String, Long> tacherIdMap = validateTeacher(teacherNames);
            for (int courseIndex = 1; courseIndex <= size; courseIndex++) {//创建课程
                Course course = new Course();
//                course.setName(courseName + courseIndex + "期");
                course.setName(courseNameList.get(courseIndex-1));
                Date currentDate = new Date();
                course.setDateBegin(new Date());
                course.setAssistantFlag(false);
                course.setCompereFlag(false);
                course.setControllerFlag(false);
                course.setSatFlag(false);
                course.setSunFlag(false);
                course.setStatus(CourseStatus.WC);
                if (null != category) {//默认直播
                    course.setCourseCategory(category);
                } else {
                    course.setCourseCategory(CourseCategory.LIVE);
                }
                course.setDateEnd(DateUtils.addDays(currentDate, 60));
                course.setExamType(examType);
                courseRepository.save(course);//创建课程
                List<ImportTeacherCourseBean> beans = map.get(courseIndex);//取出直播
                List<ImportTeacherCourseBean> lastBeans = null;
                if (courseIndex > 1) {  //不是第一节课 判断是否滚动排课n
                    lastBeans = map.get(courseIndex - 1);
                }
                for(int num=0;num<beans.size();num++){
                    ImportTeacherCourseBean bean=beans.get(num);
                    if (bean != null) {
                        CourseLive courseLive = new CourseLive();
                        courseLive.setCourseId(course.getId());
                        courseLive.setDate(bean.getDate());
                        courseLive.setName(bean.getCourseName());
                        courseLive.setTimeBegin(bean.getTimeBegin());
                        courseLive.setTimeEnd(bean.getTimeEnd());
                        courseLive.setDateInt(Integer.parseInt(DateformatUtil.format1(bean.getDate())));
                        if (courseIndex > 1&&lastBeans!=null&&!lastBeans.isEmpty()) {  //不是第一节课 判断是否滚动排课  //多条件判断
                            ImportTeacherCourseBean importTeacherCourseBean = lastBeans.get(num);
                            if(null!=importTeacherCourseBean&&importTeacherCourseBean.getTeacherName().equals(bean.getTeacherName())&&
                                    importTeacherCourseBean.getCourseName().equals(bean.getCourseName())){//相同教师
                                bean.setSourceId(importTeacherCourseBean.getSourceId());//有源课程取出源课程
                                courseLive.setSourceId(bean.getSourceId());
                            }
                        }

                        courseLive = courseLiveRepository.save(courseLive);//存储创建直播
                        if(null==bean.getSourceId()){
                            bean.setSourceId(courseLive.getId());//没有源课程将自己设为源课程
                        }
                        CourseLiveTeacher courseLiveTeacher = new CourseLiveTeacher();
                        courseLiveTeacher.setTeacherId(tacherIdMap.get(bean.getTeacherName()));//教师id
                        courseLiveTeacher.setConfirm(CourseConfirmStatus.QR);//确认
                        courseLiveTeacher.setTeacherType(TeacherType.JS);//讲师
                        courseLiveTeacher.setCourseLiveId(courseLive.getId());//直播id
                        courseLiveTeacher.setTeacherCourseLevel(TeacherCourseLevel.GOOD);//专长
                        if (StringUtils.isNotBlank(bean.getSubject())) { //科目不为空
                            List<Subject> subjectList = subjectRepository.findByName( bean.getSubject() );
                            if (null != subjectList && !subjectList.isEmpty()) {//取出的科目不为空
                                courseLiveTeacher.setSubjectId(subjectList.get(0).getId()); //设置第一个科目为指定科目
                            }
                        }
                        courseLiveTeacherRepository.save(courseLiveTeacher);
                    }
                }



            }


        }
    }

    /**
     * @description: 获取单个sheet页的所有教师
     * @author duanxiangchao
     * @date 2018/5/15 上午10:30
     */
    private Set<String> getTeacherNames(List<List<String>> dataList) {
        Set<String> teacherNames = Sets.newHashSet();
        for (int i = 1; i < dataList.size(); i++) {
            //第二行开始遍历
            List<String> strList = dataList.get(i);
            for (int j = 2; j < strList.size(); j++) {
                //第三列开始遍历
                String str = strList.get(j);
                if (StringUtils.isNotBlank(str)) {
                    try {
                        String num = str.replaceAll("[^0-9]", "");
                        String[] strings = str.split(num);
                        teacherNames.add(strings[1].trim());
                    } catch (Exception e) {
                        LOG.error("错误数据：" + str);
                        throw new BadRequestException("Excel内容错误：" + str);
                    }

                }
            }
        }
        return teacherNames;
    }

    private Set<String> getTeacherNamesDemo(List<List<String>> dataList) {
        Set<String> teacherNames = Sets.newHashSet();
        for (int i = 1; i < dataList.size(); i++) {
            //第二行开始遍历
            List<String> strList = dataList.get(i);
            //第三列开始遍历
            String str = strList.get(5);
            if (StringUtils.isNotBlank(str)) {
                try {
//                        String num = str.replaceAll("[^0-9]", "");
//                        String[] strings = str.split(num);
//                        teacherNames.add(strings[1].trim());

                    str = str.trim();
                    String[] strings = str.split("，");//中文
                    if (strings.length == 1) {
                        strings = str.split(",");//英文
                    }
                    List<String> strings1 = Arrays.asList(strings);
                    teacherNames.addAll(strings1);
                } catch (Exception e) {
                    LOG.error("错误数据：" + str);
                    throw new BadRequestException("Excel内容错误：" + str);
                }

            }
        }
        return teacherNames;
    }

    /**
     * @description: 校验教师的合法性
     * @author duanxiangchao
     * @date 2018/5/14 下午4:35
     */
    private Map<String, Long> validateTeacher(Set<String> teacherNames) {
        List<Teacher> teacherList = teacherRepository.findByNameIn(teacherNames);
        if (teacherList.size() != teacherNames.size()) {
            //教研不存在的教师
            Set<String> containTeacherName = Sets.newHashSet();
            teacherList.forEach(teacher -> {
                containTeacherName.add(teacher.getName());
            });
            teacherNames.forEach(teacherName -> {
                if (!containTeacherName.contains(teacherName)) {
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
     * @description: 获取source
     * @author duanxiangchao
     * @date 2018/5/15 下午6:53
     */
    public ImportTeacherCourseBean getTeacherCourseBean(ImportTeacherCourseBean courseBean) {
        if (courseBean.isRoll()) {
            return getTeacherCourseBean(courseBean.getTeacherCourseBean());
        } else {
            return courseBean;
        }
    }

    private CourseLive courseLiveWrapper(ImportTeacherCourseBean bean, Long courseId, Long liveRoomId) {
        CourseLive courseLive = new CourseLive();
        courseLive.setCourseId(courseId);
        courseLive.setName(bean.getCourseName());
        courseLive.setDate(bean.getDate());
        courseLive.setTimeBegin(bean.getTimeBegin());
        courseLive.setTimeEnd(bean.getTimeEnd());
        courseLive.setCourseLiveCategory(bean.getCourseLiveCategory());
        courseLive.setDateInt(Integer.parseInt(DateformatUtil.format1(bean.getDate())));
        if (bean.isRoll()) {
            courseLive.setSourceId(bean.getTeacherCourseBean().getCourseLiveId());
        }
        return courseLive;
    }

    private CourseLiveTeacher courseLiveTeacherWrapper(Long courseLiveId, Long teacherId , String subject) {
        CourseLiveTeacher courseLiveTeacher = new CourseLiveTeacher();
        if (StringUtils.isNotBlank(subject)) { //科目不为空
            List<Subject> subjectList = subjectRepository.findByName(subject );
            if (null != subjectList && !subjectList.isEmpty()) {//取出的科目不为空
                courseLiveTeacher.setSubjectId(subjectList.get(0).getId()); //设置第一个科目为指定科目
            }
        }
        courseLiveTeacher.setCourseLiveId(courseLiveId);
        courseLiveTeacher.setTeacherId(teacherId);
        courseLiveTeacher.setTeacherCourseLevel(TeacherCourseLevel.GOOD);
        courseLiveTeacher.setConfirm(CourseConfirmStatus.QR);
        courseLiveTeacher.setTeacherType(TeacherType.JS);
        return courseLiveTeacher;
    }


    @Override
    @Transactional
    public void importTeachers(List<List<List<String>>> list) {
        for (List<List<String>> sheetList : list) {//sheet
            for (List<String> data : sheetList) {//一条教师数据
                Teacher teacher = new Teacher();
                teacher.setName(data.get(1));
                teacher.setWechat(data.get(2));
                teacher.setPhone(data.get(3));
                teacher.setExamType(ExamType.GWY);//默认公务员
                teacher.setTeacherType(TeacherType.JS);//默认讲师
                try {
                    teacherRepository.save(teacher);
                } catch (Exception e) {
                    LOG.error("插入失败:" + teacher.getPhone().toString());
                }
            }
        }
    }

    @Override
    public void importTeacherByPHP(List<PHPUpdateTeacherDto> data) {
        for (PHPUpdateTeacherDto teacherDto : data) {
            try {
                Teacher teacher = new Teacher();

                Integer status = teacherDto.getStatus();
                if (status == null || status == 0) {//设置状态
                    teacher.setStatus(TeacherStatus.DSH);
                } else if (status == 1) {
                    teacher.setStatus(TeacherStatus.ZC);
                } else {
                    teacher.setStatus(TeacherStatus.JY);
                }
                String phone = teacherDto.getPhone();
                if ("".equals(phone)) {//处理空字符串
                    phone = null;
                }
                teacher.setPhone(phone);//设置电话
                teacher.setName(teacherDto.getName());//设置名字
                teacher.setPid(teacherDto.getPid());//设置phpid
                teacher.setTeacherType(TeacherType.JS);
                Teacher save = teacherRepository.save(teacher);
                Long id = save.getId();
//                if (examTypeById != null || subjectId != null) {//考试类型或科目不为空 创建
//                    TeacherSubject ts = new TeacherSubject();
//                    ts.setTeacherId(id);
//                    ts.setExamType(examTypeById);
//                    ts.setSubjectId(subjectId);
//                    ts.setTeacherCourseLevel(TeacherCourseLevel.COMMON);
//                    teacherSubjectRepository.save(ts);
//                }
            } catch (Exception e) {
                LOG.error("插入失败:" + teacherDto.toString());
                LOG.error("失败原因:" + e.getMessage());
                LOG.info("发起失败重试,去除手机号码");
                Teacher teacher = new Teacher();

                Integer status = teacherDto.getStatus();
                if (status == null || status == 0) {//设置状态
                    teacher.setStatus(TeacherStatus.DSH);
                } else if (status == 1) {
                    teacher.setStatus(TeacherStatus.ZC);
                } else {
                    teacher.setStatus(TeacherStatus.JY);
                }
                teacher.setName(teacherDto.getName());//设置名字
                teacher.setPid(teacherDto.getPid());//设置phpid
                Teacher save = teacherRepository.save(teacher);
                Long id = save.getId();
//                if (examTypeById != null || subjectId != null) {//考试类型或科目不为空 创建
//                    TeacherSubject ts = new TeacherSubject();
//                    ts.setTeacherId(id);
//                    ts.setExamType(examTypeById);
//                    ts.setSubjectId(subjectId);
//                    ts.setTeacherCourseLevel(TeacherCourseLevel.COMMON);
//                    teacherSubjectRepository.save(ts);
//                }
            }
        }
    }
}
