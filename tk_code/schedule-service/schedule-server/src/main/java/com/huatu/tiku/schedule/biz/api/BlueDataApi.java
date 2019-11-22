package com.huatu.tiku.schedule.biz.api;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.schedule.biz.domain.Course;
import com.huatu.tiku.schedule.biz.domain.CourseLive;
import com.huatu.tiku.schedule.biz.domain.Teacher;
import com.huatu.tiku.schedule.biz.enums.CourseStatus;
import com.huatu.tiku.schedule.biz.enums.TeacherStatus;
import com.huatu.tiku.schedule.biz.service.CourseLiveService;
import com.huatu.tiku.schedule.biz.service.CourseService;
import com.huatu.tiku.schedule.biz.service.TeacherService;
import com.huatu.tiku.schedule.biz.vo.php.CourseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 蓝色后台调用数据
 *
 * @author wangjian
 **/
@RestController
@RequestMapping("api/buleData")
public class BlueDataApi {

    private final CourseService courseService;

    private final CourseLiveService courseLiveService;

    private final TeacherService teacherService;

    @Autowired
    public BlueDataApi(CourseService courseService, CourseLiveService courseLiveService, TeacherService teacherService) {
        this.courseService = courseService;
        this.courseLiveService = courseLiveService;
        this.teacherService = teacherService;
    }

    /**
     * 获取课程列表 创建时间倒序
     * @return 课程集合
     */
    @RequestMapping("getCourseList")
    public List getCourseList(){
        List<Course> courses =courseService.getCourseListByStatus(CourseStatus.WC);
        ArrayList<Object> list= Lists.newArrayList();
        courses.forEach(course->
            list.add(ImmutableMap.of("id", course.getId(),"name",course.getName()))
        );
        return list;
    }

    /**
     * 获取课程详细信息
     * @param id 课程id
     */
    @RequestMapping("getCourseInfo/{id}")
    public CourseVo getCourseInfo(@PathVariable Long id){
        return new CourseVo(courseService.findOne(id));
    }

    /**
     * 获取课程直播详细信息
     *  @param ids 直播id集合
     */
    @RequestMapping("getCourseLiveInfo")
    public List getCourseLiveInfo( Long[] ids){
        if(null!=ids&&ids.length>0) {
            List<CourseLive> result = courseLiveService.findAll(Arrays.asList(ids));
            return result.stream().map(CourseVo.CourseLiveVo::new).collect(Collectors.toList());
        }else {
            return null;
        }
    }

    /**
     * 获取所有教师列表
     */
    @RequestMapping("getTeacherList")
    public List<Map> getTeacherList(){
        List<Teacher> all = teacherService.findAll();
        all=all.stream().filter(teacher -> TeacherStatus.ZC.equals(teacher.getStatus())).collect(Collectors.toList());
        ArrayList<Map> list= Lists.newArrayList();
        all.forEach(teacher-> {
                    Map<String,Object> map = Maps.newHashMap();
                    map.put("id", teacher.getId());
                    map.put("name", teacher.getName());
                    map.put("phone", teacher.getPhone());
                    map.put("type", teacher.getTeacherType());
                    map.put("status", teacher.getStatus());
                    map.put("pid", teacher.getPid());
                    list.add(map);
                }
        );
        return list;
    }

    /**
     * ids查看教师信息
     */
    @RequestMapping("getTeacherInfo/{ids}")
    public Map getTeacherInfo(@PathVariable List<Long> ids){
        List<Teacher> all = teacherService.findAll(ids);
        Map<Long,Map> resultMap=Maps.newHashMap();
        all.forEach(teacher-> {
            Map<String,Object> map = Maps.newHashMap();
            map.put("id", teacher.getId());
            map.put("name", teacher.getName());
            map.put("phone", teacher.getPhone());
            map.put("type", teacher.getTeacherType());
            map.put("status", teacher.getStatus());
            map.put("pid", teacher.getPid());
            resultMap.put(teacher.getId(),map);
        });
        return resultMap;
    }

    /**
     * pids查看教师信息
     */
    @RequestMapping("getTeacherInfoByPid/{pids}")
    public Map getTeacherInfoByPid(@PathVariable List<Long> pids){
        List<Teacher> allTeacchers = new ArrayList<>();
        pids.forEach(pid->{
            Teacher teacher = teacherService.findByPid(pid);
            if(null!=teacher){
                allTeacchers.add(teacher);
            }
        });
        Map<Long,Map> resultMap=Maps.newHashMap();
        allTeacchers.forEach(teacher-> {
            HashMap<String,Object> map = Maps.newHashMap();
            map.put("id", teacher.getId());
            map.put("name", teacher.getName());
            map.put("phone", teacher.getPhone());
            map.put("type", teacher.getTeacherType());
            map.put("status", teacher.getStatus());
            map.put("pid", teacher.getPid());
            resultMap.put(teacher.getPid(),map);
        });
        return resultMap;
    }
}
