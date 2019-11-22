package com.huatu.splider.task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import com.huatu.splider.dao.jpa.entity.FbCourse;
import com.huatu.splider.dao.jpa.entity.FbCourseSet;
import com.huatu.splider.dao.jpa.entity.FbCourseSnapshot;
import com.huatu.splider.dao.jpa.entity.FbTeacher;
import com.huatu.splider.service.FbCourseService;
import com.huatu.splider.service.FbCourseSetService;
import com.huatu.splider.service.FbCourseSnapshotService;
import com.huatu.splider.service.FbTeacherService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hanchao
 * @date 2018/2/27 16:43
 */
@Configuration
@Slf4j
public class FbCourseTask {
    //所有的课程种类
    private static final List<String> courseTypes = Lists.newArrayList("gwy","zj","kuaiji","yingyu","kaoyan","it","jzs","sikao","yixue");

    private static Set<Integer> teachersCache = Sets.newHashSet();
    private static volatile int version;

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private FbCourseService fbCourseService;
    @Autowired
    private FbTeacherService fbTeacherService;
    @Autowired
    private FbCourseSnapshotService fbCourseSnapshotService;
    @Autowired
    private FbCourseSetService fbCourseSetService;

   // @Scheduled(cron = "0/3 * * * * ?")
public void a(){
    log.info("run  ");
}

    @Scheduled(cron = "0 0/30 * * * ?")
    public void execute(){
    log.info("start execute");
        prepareBasicData();
        fbCourseService.deleteAll();//更新所有课程，以便于控制上下架
        fbCourseSetService.deleteAll();
        for (String courseType : courseTypes) {
            try {
                String url = "http://ke.fenbi.com/android/{courseType}/v3/content?cat=0&start=0&len=10000&platform=android24&version=6.4.3&vendor=Tencent&app=gwy&deviceId=OrhZlzbYThpUUiT1QBqO6A==&av=8&kav=3";
                String responseStr = restTemplate.getForObject(url, String.class, courseType);

                JSONObject data = JSONObject.parseObject(responseStr);
                JSONArray courses = data.getJSONArray("datas");
                for (Object courseObj : courses) {
                    JSONObject course = (JSONObject) courseObj;
                    Integer contentType = course.getInteger("contentType");
                    if(Objects.equals(0,contentType)){
                        JSONObject lecture = course.getJSONObject("lectureSummary");
                        //普通课程
                        try {
                            dealCourseInfo(lecture,courseType);//处理课程基本信息
                        } catch(Exception e){
                            e.printStackTrace();
                        }

                        try {
                            dealCourseSaleInfo(lecture);
                        } catch(Exception e){
                            e.printStackTrace();
                        }
                    }else if(Objects.equals(3,contentType)){
                        //合集
                        dealCourseSetInfo(course,courseType);
                    }
                }


            } catch(Exception e){
                log.error("catch {} courses get an error",courseType,e);
            }
        }
    }

    private void prepareBasicData(){
        teachersCache.clear();
        String versionStr = FastDateFormat.getInstance("yyMMddHHmm").format(new Date());
        version = Optional.ofNullable(Ints.tryParse(versionStr)).orElse(0);
    }


    private void dealCourseSetInfo(JSONObject courseSet,String courseType){
        FbCourseSet fbCourseSet = new FbCourseSet();
        fbCourseSet.setId(courseSet.getInteger("id"));
        fbCourseSet.setCourseType(courseType);
        JSONObject lectureSetSummary = courseSet.getJSONObject("lectureSetSummary");
        fbCourseSet.setTitle(lectureSetSummary.getString("title"));
        fbCourseSet.setTag(lectureSetSummary.getString("tag"));
        fbCourseSet.setStudentLimit(lectureSetSummary.getInteger("studentLimit"));
        fbCourseSet.setStudentCount(lectureSetSummary.getInteger("studentCount"));
        fbCourseSet.setFloorPrice(lectureSetSummary.getBigDecimal("floorPrice"));
        fbCourseSet.setTopPrice(lectureSetSummary.getBigDecimal("topPrice"));
        fbCourseSet.setSaleStatus(lectureSetSummary.getInteger("saleStatus"));
        fbCourseSet.setClassStartTime(lectureSetSummary.getDate("classStartTime"));
        fbCourseSet.setClassStopTime(lectureSetSummary.getDate("classStopTime"));
        fbCourseSet.setStartSaleTime(lectureSetSummary.getDate("startSaleTime"));
        fbCourseSet.setStopSaleTime(lectureSetSummary.getDate("stopSaleTime"));
        fbCourseSet.setTeachChannel(lectureSetSummary.getInteger("teachChannel"));
        fbCourseSet.setAvailableOunt(lectureSetSummary.getInteger("availableCount"));
        fbCourseSet.setTradeUnit(lectureSetSummary.getInteger("tradeUnit"));
        fbCourseSet.setRawData(courseSet.toJSONString());
        fbCourseSet.setUpdateTime(new Date());
        fbCourseSet.setState(true);



        JSONArray teachers = lectureSetSummary.getJSONArray("teachers");
        List<FbTeacher> teacherList = dealTeachers(teachers);

        fbCourseSet.setTeachers(StringUtils.join(teacherList.stream().map(FbTeacher::getId).collect(Collectors.toList()),","));

        List<FbCourse> fbCourseList = Lists.newArrayList();
        try {
            String url = "http://ke.fenbi.com/android/{courseType}/v3/lecturesets/{courseSetId}/lectures?start=0&len=10&platform=android24&version=6.4.3&vendor=Tencent&app=gwy&deviceId=OrhZlzbYThpUUiT1QBqO6A==&av=8&kav=3";
            String responseStr = restTemplate.getForObject(url, String.class, courseType,fbCourseSet.getId());
            JSONObject data = JSONObject.parseObject(responseStr);
            JSONArray courses = data.getJSONArray("datas");


            for (Object courseObj : courses) {
                JSONObject course = (JSONObject) courseObj;
                FbCourse fbCourse = dealCourseInfo(course,courseType);
                fbCourseList.add(fbCourse);
                dealCourseSaleInfo(course);
                //
            }

        } catch(Exception e){
            e.printStackTrace();
        }


        fbCourseSet.setCourses(StringUtils.join(fbCourseList.stream().map(FbCourse::getId).collect(Collectors.toList()),","));
        fbCourseSetService.save(fbCourseSet);

    }


    private List<FbTeacher> dealTeachers(JSONArray teachers){
        List<FbTeacher> teacherList = Lists.newArrayList();
        if(teachers != null){
            for (Object teacherObj : teachers) {
                JSONObject teacher = (JSONObject) teacherObj;
                FbTeacher fbTeacher = new FbTeacher();
                fbTeacher.setId(teacher.getInteger("id"));
                fbTeacher.setUserId(teacher.getInteger("userId"));
                fbTeacher.setName(teacher.getString("name"));
                fbTeacher.setBrief(teacher.getString("brief"));
                fbTeacher.setDesc(teacher.getString("desc"));
                fbTeacher.setAvatar("http://ke.fbstatic.cn/api/images/"+teacher.getString("avatar"));
                fbTeacher.setRawData(teacher.toJSONString());
                teacherList.add(fbTeacher);

            }
            List<FbTeacher> collect = teacherList.stream().filter(x -> !teachersCache.contains(x.getId())).collect(Collectors.toList());
            fbTeacherService.save(collect);

            teachersCache.addAll(collect.stream().map(FbTeacher::getId).collect(Collectors.toSet()));
        }
        //存储老师
        return teacherList;
    }

    private FbCourse dealCourseInfo(JSONObject lectureSummary,String courseType){
        FbCourse fbCourse = new FbCourse();
        fbCourse.setId(lectureSummary.getInteger("id"));


        fbCourse.setTitle(lectureSummary.getString("title"));
        fbCourse.setCourseType(courseType);
        fbCourse.setStatus(lectureSummary.getInteger("status"));
        fbCourse.setSaleStatus(lectureSummary.getInteger("saleStatus"));
        fbCourse.setSalesStatus(lectureSummary.getInteger("salesStatus"));
        fbCourse.setBrief(lectureSummary.getString("brief"));
        fbCourse.setPrice(lectureSummary.getBigDecimal("price"));
        fbCourse.setStudentLimit(lectureSummary.getInteger("studentLimit"));
        fbCourse.setStudentCount(lectureSummary.getInteger("studentCount"));
        fbCourse.setHasAddress(lectureSummary.getBoolean("hasAddress"));
        fbCourse.setClassHours(lectureSummary.getInteger("classHours"));
        fbCourse.setStartSaleTime(lectureSummary.getDate("startSaleTime"));
        fbCourse.setStopSaleTime(lectureSummary.getDate("stopSaleTime"));
        fbCourse.setClassStartTime(lectureSummary.getDate("classStartTime"));
        fbCourse.setClassEndTime(lectureSummary.getDate("classEndTime"));
        fbCourse.setHasExercise(lectureSummary.getBoolean("hasExercise"));
        fbCourse.setHasQqGroup(lectureSummary.getBoolean("hasQQGroup"));
        fbCourse.setHasImGroup(lectureSummary.getBoolean("hasImGroup"));
        fbCourse.setHasClassPeriod(lectureSummary.getBoolean("hasClassPeriod"));
        fbCourse.setHasLiveEpisodes(lectureSummary.getBoolean("hasLiveEpisodes"));
        fbCourse.setUpdateTime(new Date());
        fbCourse.setRawData(lectureSummary.toJSONString());
        fbCourse.setState(true);

        JSONArray teachers = lectureSummary.getJSONArray("teachers");

        List<FbTeacher> teacherList = dealTeachers(teachers);

        fbCourse.setTeachers(StringUtils.join(teacherList.stream().map(FbTeacher::getId).collect(Collectors.toList()),","));


        fbCourseService.save(fbCourse);

        return fbCourse;
    }

    private void dealCourseSaleInfo(JSONObject lectureSummary){
        //
        int courseId = lectureSummary.getInteger("id");

        FbCourseSnapshot lastSnapshot = fbCourseSnapshotService.getLastestSnapshot(courseId);


        FbCourseSnapshot snapshot = new FbCourseSnapshot();
        snapshot.setCourseId(courseId);
        snapshot.setTitle(lectureSummary.getString("title"));
        snapshot.setPrice(lectureSummary.getBigDecimal("price"));
        snapshot.setStudentLimit(lectureSummary.getInteger("studentLimit"));
        snapshot.setStudentCount(lectureSummary.getInteger("studentCount"));
        if(lastSnapshot != null){
            snapshot.setPreviousCount(lastSnapshot.getStudentCount());
            snapshot.setPrevioursPrice(lastSnapshot.getPrice());
            snapshot.setCurrentCycleIncome(snapshot.getPrice().multiply(new BigDecimal(snapshot.getStudentCount() - lastSnapshot.getStudentCount())));
        }else{
            snapshot.setCurrentCycleIncome(snapshot.getPrice().multiply(new BigDecimal(snapshot.getStudentCount())));
        }
        snapshot.setVersion(version);
        snapshot.setCreateTime(new Date());

        fbCourseSnapshotService.save(snapshot);
    }


}
