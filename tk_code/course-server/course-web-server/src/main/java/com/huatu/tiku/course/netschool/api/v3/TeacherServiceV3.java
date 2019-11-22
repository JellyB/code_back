package com.huatu.tiku.course.netschool.api.v3;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author hanchao
 * @date 2017/9/20 10:10
 */
@FeignClient(value = "course-service")
public interface TeacherServiceV3 {
    /**
     * 获取老师介绍
     * @param teacherid
     * @return
     */
    @GetMapping("/v3/teacher_Introduction.php?action=info")
    NetSchoolResponse getTeacherInfo(@RequestParam("teacherid")int teacherid);

    /**
     * 获取老师在售课程
     * @param teacherid
     * @return
     */
    @GetMapping("/v3/teacher_Introduction.php?action=course")
    NetSchoolResponse findTeacherCourses(@RequestParam("teacherid")int teacherid,@RequestParam("page")int page);

    /**
     * 获取老师历史课程得分
     * @param teacherid
     * @return
     */
    @GetMapping("/v3/teacher_Introduction.php?action=class")
    NetSchoolResponse findTeacherCourseScores(@RequestParam("teacherid")int teacherid,@RequestParam("page")int page);


    /**
     * 老师综合得分
     * @param teacherid
     * @return
     */
    @GetMapping("/v3/teacher_Introduction.php?action=assess")
    NetSchoolResponse getTeacherScore(@RequestParam("teacherid")int teacherid);
}
