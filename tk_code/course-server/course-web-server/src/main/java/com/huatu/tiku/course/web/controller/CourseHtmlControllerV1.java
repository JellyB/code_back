package com.huatu.tiku.course.web.controller;

import com.huatu.tiku.course.netschool.api.HtmlServiceV1;
import com.huatu.tiku.course.service.CourseBizService;
import com.huatu.tiku.course.service.IosBffService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author hanchao
 * @date 2017/8/30 11:48
 */
@RestController
@RequestMapping(value = "v1/courses")
@Slf4j
public class CourseHtmlControllerV1 {
    @Autowired
    private IosBffService bffService;

    @Autowired
    private HtmlServiceV1 htmlService;

    @Autowired
    private CourseBizService courseBizService;
    /**
     * h5页面
     * @param courseId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "{courseId}",produces = MediaType.TEXT_HTML_VALUE+ ";charset=UTF-8")
    public Object detail(@PathVariable int courseId) throws Exception{
        return courseBizService.getCourseHtml(courseId);
    }


    /**
     * ios审核h5页面
     * @param courseId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "static/{courseId}",produces = MediaType.TEXT_HTML_VALUE+ ";charset=UTF-8")
    public Object auditDetail(@PathVariable int courseId) throws Exception{
        return bffService.getTemplateByName(courseId+".html");
    }
}
