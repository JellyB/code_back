package com.huatu.ztk.backend.metas.controller;

import com.huatu.ztk.backend.metas.service.PracticeMetaService;
import com.huatu.ztk.backend.util.ExcelManageUtil;
import com.huatu.ztk.backend.util.FunFileUtils;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.commons.exception.SuccessMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by huangqp on 2018\6\14 0014.
 */
@RestController
@RequestMapping(value = "meta/course", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class CourseMetaController {
    @Autowired
    PracticeMetaService practiceMetaService;
    @RequestMapping(value = "userInfo" , method = RequestMethod.GET)
    public Object assertCourseUserPracticeMeta(@RequestParam String path){
        String name = "userCourse_"+System.currentTimeMillis();
        String result = FunFileUtils.TMP_EXCEL_SOURCE_FILEPATH+name+".xls";
        try {
            List<List> list = ExcelManageUtil.readExcel(FunFileUtils.TMP_EXCEL_SOURCE_FILEPATH+path);
            practiceMetaService.assertCourseUserPracticeMeta(list,name);
        } catch (Exception e) {
            e.printStackTrace();
            return ErrorResult.create(1002212,"失败了！"+e.getMessage());
        }
        return SuccessMessage.create(result);
    }
}

