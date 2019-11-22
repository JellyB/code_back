package com.huatu.ztk.backend.subject.controller;

import com.huatu.ztk.backend.subject.bean.SubjectBean;
import com.huatu.ztk.backend.subject.service.CatgoryService;
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
@RequestMapping("/catgory")
public class CatgoryController {

    private static final Logger logger = LoggerFactory.getLogger(CatgoryController.class);

    @Autowired
    private CatgoryService catgoryService;

    /**
     * 获取列表
     *
     * @return
     */
    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object list() {
        return catgoryService.findAll();
    }

    /**
     * 获取单个
     *
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object findById(@RequestParam  int id) throws BizException{
        return catgoryService.findById(id);
    }


    /**
     * 删除
     *
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void delete(@RequestParam int id) throws BizException{
        catgoryService.delete(id);
    }


    /**
     * 更新
     *
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void update(@RequestBody SubjectBean subjectBean) throws BizException{
        catgoryService.update(subjectBean);
    }


    /**
     * 新增
     *
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void add(@RequestBody SubjectBean subjectBean, HttpServletRequest request) throws BizException {
        long uid = UserService.getUserId(request);
        subjectBean.setCreateBy(uid);
        catgoryService.insert(subjectBean);
    }
}
