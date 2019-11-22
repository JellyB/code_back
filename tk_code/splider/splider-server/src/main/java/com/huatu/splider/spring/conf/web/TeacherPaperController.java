package com.huatu.splider.spring.conf.web;

import com.huatu.splider.service.FbTeacherPaperService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by x6 on 2018/5/30.
 */
@RestController
@RequestMapping("/teacher")
@Slf4j
public class TeacherPaperController {

    @Autowired
    FbTeacherPaperService fbTeacherPaperService;
    /**
     * 查询试卷列表
     */
    @PostMapping(value="list",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object list(){
         fbTeacherPaperService.getList();
         return null;
    }


}
