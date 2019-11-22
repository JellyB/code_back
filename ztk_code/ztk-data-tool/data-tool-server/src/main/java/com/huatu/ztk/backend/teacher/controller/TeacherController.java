package com.huatu.ztk.backend.teacher.controller;

import com.huatu.ztk.backend.teacher.bean.UrlBean;
import com.huatu.ztk.backend.teacher.service.TeacherService;
import com.huatu.ztk.backend.teacher.service.avatarUpload;
import com.huatu.ztk.commons.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Author: xuhuiqiang
 * Time: 2017-05-12  10:46 .
 */
@RestController
@RequestMapping("/teacher")
public class TeacherController {
    private static final Logger logger = LoggerFactory.getLogger(TeacherController.class);

    @Autowired
    private TeacherService teacherService;
    @Autowired
    private avatarUpload avatar;

    /**
     * 查找所有教师
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Object teacherList(){
        return teacherService.findAllTeacher();
    }

    /**
     * 根据名字，查找老师
     * @param name
     * @return
     */
    @RequestMapping(value = "/findTeacherByName", method = RequestMethod.GET)
    public Object findTeacherByName(@RequestParam String name){
        return teacherService.findTeacherByName(name);
    }

    /**
     * 根据id，查找老师
     * @param id
     * @return
     */
    @RequestMapping(value = "/findTeacherById", method = RequestMethod.GET)
    public Object findTeacherById(@RequestParam int id){
        return teacherService.findTeacherById(id);
    }

    /**
     * 根据id，删除老师
     * @param id
     */
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public void deleteTeacher(@RequestParam int id){
        teacherService.delete(id);
    }

    /**
     * 根据传输过来的json串，新增老师信息
     * @param str
     */
    @RequestMapping(value = "/insert", method = RequestMethod.PUT)
    public void insertTeacher(@RequestBody String str){
        teacherService.insert(str);
    }

    /**
     * 根据传输过来的json串，编辑老师信息
     * @param str
     */
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public void editTeacher(@RequestBody String str){
        teacherService.edit(str);
    }

    /**
     * 上传图片到zimg上，并返回地址
     */
    @RequestMapping(value = "/upload", method = RequestMethod.PUT)
    public Object upload(@RequestBody String str) throws IOException, BizException {
        String url = avatar.upload(str);
        logger.info("返回的结果={}",url);
        UrlBean urlBean = UrlBean.builder()
                .url(url)
                .build();
        return urlBean;
    }

}
