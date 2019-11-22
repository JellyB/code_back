package com.huatu.tiku.banckend.controller;

import com.huatu.tiku.banckend.service.CourseQuestionImportService;
import com.huatu.tiku.dto.request.CourseQuestionImportVO;
import com.huatu.tiku.util.log.LogPrint;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.SuccessMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

import static com.huatu.ztk.commons.exception.CommonErrors.INVALID_ARGUMENTS;


/**
 * 课件关联试题批量导入
 * Created by zhaoxi
 */
@RestController
@RequestMapping("/backend/course/question/")
public class CourseQuestionImportController {

    @Autowired
    CourseQuestionImportService courseQuestionImportService;

    /**
     * 课件关联试题批量导入
     * @return
     */
    @LogPrint
    @PostMapping(value = "batch")
    public Object batch(@RequestBody CourseQuestionImportVO vo, @RequestHeader Long userId) throws BizException{


        courseQuestionImportService.batchImport(vo, userId);

        //创建任务
        return SuccessMessage.create("试题数据导入成功");
    }


    /**
     * 根据考试类型查询
     * @return
     */
    @LogPrint
    @GetMapping(value = "subject")
    public Object getAllSubject() throws BizException{

        return courseQuestionImportService.getAllSubject();

    }

    /**
     * 课件试题信息同步(直播随堂练习)
     * @return
     */
    @LogPrint
    @PostMapping(value = "synchronize")
    public Object synchronizeCourse(@RequestParam("courseIds") String courseIds,@RequestHeader Long userId) throws BizException{

        if(StringUtils.isEmpty(courseIds)){
            throw new BizException(INVALID_ARGUMENTS);
        }
        courseQuestionImportService.synchronizeCourse(courseIds, userId);
        return SuccessMessage.create("试题信息同步成功");
    }

}
