package com.huatu.tiku.banckend.controller;

import com.huatu.common.SuccessMessage;
import com.huatu.tiku.banckend.service.impl.AsyncTaskServiceImpl;
import com.huatu.tiku.banckend.service.CourseBreakpointService;
import com.huatu.tiku.entity.CourseBreakpoint;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 课程-断点管理接口
 * Created by lijun on 2018/6/11
 */
@RestController
@RequestMapping("/backend/courseBreakpoint")
public class CourseBreakpointController {

    @Autowired
    private CourseBreakpointService service;

    @Autowired
    private AsyncTaskServiceImpl asyncTaskServiceImpl;

    /**
     * 编辑数据
     */
    @PostMapping(value = "edit")
    public Object edit(
            @Validated @RequestBody CourseBreakpoint courseBreakpoint
    ) {
        service.save(courseBreakpoint);
        //异步，更新php端练习数量
        asyncTaskServiceImpl.upQuestionNumOfCourse(courseBreakpoint.getCourseType(), courseBreakpoint.getCourseId());
        return SuccessMessage.create();
    }

    /**
     * 列表查询
     *
     * @param pointName
     * @return
     */
    @GetMapping(value = "/{courseType}/{courseId}")
    public Object list(
            @PathVariable("courseType") Integer courseType,
            @PathVariable("courseId") long courseId,
            @RequestParam(value = "pointName", required = false) String pointName,
            @RequestParam(value = "position", defaultValue = "0") long position
    ) {
        List<CourseBreakpoint> breakpointList = service.listData(courseType, courseId, pointName, position);
        return breakpointList;
    }

    /**
     * 删除一个点信息
     *
     * @return
     */
    @DeleteMapping(value = "/{idList}")
    public Object delete(
            @PathVariable("idList") String idList
    ) {
        List<String> idLongList;
        if (StringUtils.isNotBlank(idList) && (idLongList = Arrays.asList(idList.split(","))).size() > 0) {

            CourseBreakpoint breakpoint = CourseBreakpoint.builder().id(Long.parseLong(idLongList.get(0))).build();
            CourseBreakpoint courseBreakpoint = service.selectOne(breakpoint);

            service.deleteByIdList(idLongList.stream().map(Long::valueOf).collect(Collectors.toList()));
            //异步，更新php端练习数量
            asyncTaskServiceImpl.upQuestionNumOfCourse(courseBreakpoint.getCourseType(), courseBreakpoint.getCourseId());
        }
        return SuccessMessage.create();
    }

    /**
     * 需要修改位置的数据
     *
     * @param list 修改集合
     * @return
     */
    @PostMapping(value = "changeSort")
    public Object changeSort(
            @RequestBody List<CourseBreakpoint> list
    ) {
        service.changeSort(list);
        return SuccessMessage.create();
    }

}
