package com.huatu.tiku.banckend.controller;

import com.huatu.common.SuccessMessage;
import com.huatu.tiku.banckend.service.CourseKnowledgeService;
import com.huatu.tiku.dto.request.BatchKnowledgeByCourseReqVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by lijun on 2018/6/11
 */
@RestController
@RequestMapping("/backend/courseKnowledge")
public class CourseKnowledgeController {

    @Autowired
    private CourseKnowledgeService service;

    /**
     * 编辑数据
     */
    @PostMapping(value = "edit")
    public Object editCourseKnowledge(
            @RequestParam("courseId") long courseId,
            @RequestParam("courseType") Integer courseType,
            @RequestParam("knowledgePointIds") String knowledgePointIds
    ) {
        service.edit(courseType, courseId, knowledgePointIds);
        return SuccessMessage.create();
    }

    /**
     * 通过课程ID 获取知识点信息
     */
    @GetMapping(value = "/{courseType}/{courseId}")
    public Object getListByCourseId(
            @PathVariable("courseType") Integer courseType,
            @PathVariable("courseId") long courseId
    ) {
        return service.getListByCourseId(courseType, courseId);
    }

    /**
     * 根据课程id 清除该课程的所有关联信息
     */
    @DeleteMapping(value = "/{courseType}/{courseId}")
    public Object deleteByCourseId(
            @PathVariable("courseType") Integer courseType,
            @PathVariable("courseId") long courseId
    ) {
        service.removeAllByCourseId(courseType, courseId);
        return SuccessMessage.create();
    }


    /**
     * 通过课程ID 获取知识点信息（批量）
     */
    @PostMapping(value = "batch")
    public Object getBatchListByCourseId(@RequestBody List<BatchKnowledgeByCourseReqVO> list) {
        return service.getBatchListByCourseId(list);
    }

}
