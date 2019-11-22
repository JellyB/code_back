package com.huatu.ztk.backend.subject.controller;

import com.huatu.ztk.backend.subject.bean.SubjectBean;
import com.huatu.ztk.backend.subject.service.SubjectService;
import com.huatu.ztk.backend.user.service.UserService;
import com.huatu.ztk.commons.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by ht on 2016/12/21.
 */
@RestController
@RequestMapping("/subject")
public class SubjectController {

    private static final Logger logger = LoggerFactory.getLogger(SubjectController.class);

    @Autowired
    private SubjectService subjectService;

    /**
     * 获取列表
     *
     * @param catgory 0表示全部
     * @return
     */
    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object list(@RequestParam(defaultValue = "0") int catgory, HttpServletRequest request) throws BizException{
        long userId = UserService.getUserId(request);
        return subjectService.findList(catgory, userId);
    }


    /**
     * 获取单个
     *
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object findById(@RequestParam  int id) throws BizException{
        return subjectService.findById(id);
    }


    /**
     * 删除
     *
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void delete(@RequestParam int id) throws BizException{
        subjectService.delete(id);
    }


    /**
     * 更新
     *
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void update(@RequestBody SubjectBean subjectBean, HttpServletRequest request) throws BizException{
        long uid = UserService.getUserId(request);
        subjectBean.setCreateBy(uid);
        subjectService.update(subjectBean);
    }


    /**
     * 新增
     *
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void add(@RequestBody SubjectBean subjectBean, HttpServletRequest request) throws BizException{
        long uid = UserService.getUserId(request);
        subjectBean.setCreateBy(uid);
        subjectService.insert(subjectBean);
    }

    /**
     * 所有考试类型下的考试科目 id->(id,name)
     */
    @RequestMapping(value = "total", method = RequestMethod.GET)
    public Object getTotalSubject() throws BizException {
        return subjectService.getTotalMap();
    }

    /**
     *获得用户可以操作的考试类型
     * @param request
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "user",method = RequestMethod.GET)
    public Object getUserCatgorys(HttpServletRequest request) throws BizException{
        long uid = UserService.getUserId(request);

        return subjectService.getUserCatgories(uid);
    }

    @RequestMapping(value = "all",method = RequestMethod.GET)
    public Object getSubjectList(@RequestParam(defaultValue = "-1") int category) throws BizException{
        return subjectService.findAll(category);
    }

}
