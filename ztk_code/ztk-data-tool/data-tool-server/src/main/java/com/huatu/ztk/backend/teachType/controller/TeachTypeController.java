package com.huatu.ztk.backend.teachType.controller;

import com.huatu.ztk.backend.teachType.bean.TeachTypeBean;
import com.huatu.ztk.backend.teachType.service.TeachTypeService;
import com.huatu.ztk.backend.user.bean.User;
import com.huatu.ztk.backend.user.service.UserService;
import com.huatu.ztk.commons.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Created by ht on 2016/12/21.
 */
@RestController
@RequestMapping("/teachType")
public class TeachTypeController {

    private static final Logger logger = LoggerFactory.getLogger(TeachTypeController.class);

    @Autowired
    private TeachTypeService teachTypeService;


    /**
     * 获取列表
     *
     * @param catgory 0表示全部
     * @return
     */
    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object list(@RequestParam(defaultValue = "0") int catgory, HttpServletRequest request) throws BizException {
        long userId = UserService.getUserId(request);
        return teachTypeService.findList(catgory, userId);
    }


    /**
     * 获取单个
     *
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object findById(@RequestParam  int id) throws BizException{
        return teachTypeService.findById(id);
    }


    /**
     * 删除
     *
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void delete(@RequestParam int id) throws BizException{
        teachTypeService.delete(id);
    }


    /**
     * 更新
     *
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void update(@RequestBody TeachTypeBean teachTypeBean, HttpServletRequest request) throws BizException{
        long uid = getUid(request);
        teachTypeBean.setCreateBy(uid);
        teachTypeService.update(teachTypeBean);
    }


    /**
     * 新增
     *
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void add(@RequestBody TeachTypeBean teachTypeBean, HttpServletRequest request) throws BizException{
        long uid = getUid(request);
        teachTypeBean.setCreateBy(uid);
        teachTypeService.insert(teachTypeBean);
    }

    public long getUid(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        User user = (User) session.getAttribute("user");

        long userId = user != null ? user.getId() : 0;
        return userId;
    }
}
