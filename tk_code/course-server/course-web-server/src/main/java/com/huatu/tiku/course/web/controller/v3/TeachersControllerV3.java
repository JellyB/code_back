package com.huatu.tiku.course.web.controller.v3;

import com.huatu.tiku.course.netschool.api.v3.TeacherServiceV3;
import com.huatu.tiku.course.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author hanchao
 * @date 2017/9/20 10:09
 */
@RequestMapping("/v3/teachers")
@RestController
public class TeachersControllerV3 {
    @Autowired
    private TeacherServiceV3 teacherServiceV3;

    /**
     * 老师介绍
     * @param tid
     * @return
     */
    @GetMapping("/{tid}")
    public Object getTeacherInfo(@PathVariable int tid) {
        return ResponseUtil.build(teacherServiceV3.getTeacherInfo(tid));
    }



    /**
     * 在售课程列表
     * @param tid
     * @param page
     * @return
     */
    @GetMapping("/{tid}/courses")
    public Object findTeacherCourses(@PathVariable int tid,
                                    @RequestParam int page) {
        return ResponseUtil.build(teacherServiceV3.findTeacherCourses(tid,page));
    }

    /**
     * 老师历史得分
     * @param tid
     * @param page
     * @return
     */
    @GetMapping("/{tid}/score/history")
    public Object findTeacherScores(@PathVariable int tid,
                                    @RequestParam int page) {
        return ResponseUtil.build(teacherServiceV3.findTeacherCourseScores(tid,page));
    }



    /**
     * 老师综合得分
     * @param tid
     * @return
     */
    @GetMapping("/{tid}/score")
    public Object getTeacherScore(@PathVariable int tid) {
        return ResponseUtil.build(teacherServiceV3.getTeacherScore(tid));
    }


}
