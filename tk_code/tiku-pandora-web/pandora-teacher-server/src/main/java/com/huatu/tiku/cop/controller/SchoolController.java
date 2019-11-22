package com.huatu.tiku.cop.controller;

import com.huatu.tiku.cop.service.SchoolService;
import com.huatu.tiku.teacher.service.paper.PaperActivityImportDataService;
import com.huatu.tiku.util.log.LogPrint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author zhaoxi
 * @Description: 公安招警-院校管理相关接口
 * @date 2018/8/17下午3:18
 */
@RestController
@RequestMapping("/school")
@Slf4j
public class SchoolController {

    @Autowired
    private SchoolService schoolService;

    @Autowired
    private PaperActivityImportDataService paperActivityImportDataService;

//    /**
//     * 查询所有地区和学院列表
//      * @return
//     */
//    @LogPrint
//    @GetMapping("all")
//    public Object getAreaList() {
//
//        return schoolService.findAreaList();
//    }


    /**
     * 查询院校信息
     *
     * @return
     */
    @LogPrint
    @GetMapping("")
    public Object getSchoolListByName() {

        return schoolService.findSchoolList();
    }


    /**
     * 导出机考数据
     *
     * @return
     */
    @LogPrint
    @GetMapping("data")
    public ModelAndView importData(@RequestParam(defaultValue = "") int paperId) {
        return schoolService.importData(paperId);
    }

    /**
     * 导出行测数据（临时接口）
     *
     * @return
     */
    @LogPrint
    @GetMapping("line/data")
    public ModelAndView importLineData(@RequestParam(defaultValue = "") int paperId) {
        return schoolService.importLineData(paperId);
    }

    /**
     * 导出模考数据
     *
     * @return
     */
    @LogPrint
    @GetMapping("mock")
    public ModelAndView importMockData(@RequestParam(defaultValue = "") int subject) {
        return schoolService.importMockData(subject);
    }


    /**
     * 导出行测除真题演练外的其他活动,学员参加信息
     *
     * @return
     */
    @LogPrint
    @GetMapping("userExamData")
    public ModelAndView importUserExamData(@RequestParam(defaultValue = "") int paperId) {
        return paperActivityImportDataService.importUserExamData(paperId);
    }



}
