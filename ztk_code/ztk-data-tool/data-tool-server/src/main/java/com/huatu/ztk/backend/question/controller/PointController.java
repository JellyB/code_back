package com.huatu.ztk.backend.question.controller;

import com.huatu.ztk.backend.question.service.PointService;
import com.huatu.ztk.backend.user.bean.User;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Author: xuhuiqiang
 * Time: 2017-02-07  15:42 .
 */
@RestController
@RequestMapping(value = "/question", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class PointController {
    private static final Logger logger = LoggerFactory.getLogger(PointController.class);
    @Autowired
    private PointService pointService;

    /**
     * 根据name、parentId、level，插入知识点
     * @param str
     * @return
     */
    @RequestMapping(value = "point/add", method = RequestMethod.POST)
    public Object pointAdd(@RequestBody String str) {
        logger.info("point add json={}", JsonUtil.toJson(str));
        return pointService.addPoint(str);
    }

    /**
     * 根据id，name，修改知识点，修改成功返回1，失败返回0
     */
    @RequestMapping(value = "point/edit", method = RequestMethod.GET)
    public Object pointEdit(@RequestParam int id,
                            @RequestParam String name) {
        logger.info("point add id={} name={}", id,name);
        return pointService.editPoint(id,name);
    }

    /**
     * 根据id，删除知识点
     * @param pointId
     */
    @RequestMapping(value = "point/delete", method = RequestMethod.GET)
    public Object pointDelete(@RequestParam int pointId)  {
        return pointService.deletePoint(pointId);
    }

    @RequestMapping(value = "point/question/update" ,method = RequestMethod.POST)
    public Object updateQuestionPoint(@RequestParam int id)  {
        pointService.updateQuestionPoint(id);
        return null;
    }

    @RequestMapping(value = "point/all", method = RequestMethod.GET)
    public Object pointsFind(@RequestParam(defaultValue = "-1") int subject ) {
        return pointService.findAll(subject);
    }
}
