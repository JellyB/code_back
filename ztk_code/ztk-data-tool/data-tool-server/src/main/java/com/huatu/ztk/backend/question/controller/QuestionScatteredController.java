package com.huatu.ztk.backend.question.controller;

import com.huatu.ztk.backend.question.dao.QuestionScatteredDao;
import com.huatu.ztk.backend.question.service.QuestionScatteredService;
import com.huatu.ztk.backend.user.bean.User;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.question.exception.IllegalQuestionException;
import com.itextpdf.text.BadElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2017-06-15  15:55 .
 */
@RestController
@RequestMapping(value = "/questionScattered", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class QuestionScatteredController {
    private static final Logger logger = LoggerFactory.getLogger(QuestionScatteredController.class);

    @Autowired
    private QuestionScatteredService questionScatteredService;

    @RequestMapping(value = "common/list", method = RequestMethod.GET)
    public Object questionListByDetail(@RequestParam int subject,
                                       @RequestParam String stem,
                                       @RequestParam List<Integer> pointId,
                                       @RequestParam long startTime,
                                       @RequestParam long endTime,
                                       @RequestParam int flag,
                                       @RequestParam int status,
                                       @RequestParam int module,
                                       HttpServletRequest request){
        final HttpSession session = request.getSession(true);
        User user = (User) session.getAttribute("user");
        int uid = -1;
        if(user!=null){
            uid =(int) user.getId();
        }
        return questionScatteredService.findByDetail(subject,stem,pointId,startTime,endTime,flag,uid,status,module);
    }


    /**
     * 试题发布
     * @param id
     * @throws IllegalQuestionException
     */
    @RequestMapping(value = "common/release", method = RequestMethod.PUT)
    public void releaseById(@RequestParam int id) throws IllegalQuestionException {
        questionScatteredService.release(id);
    }

    /**
     * 试题审核
     * @param id
     * @param description
     * @param type
     * @throws IllegalQuestionException
     */
    @RequestMapping(value = "common/review", method = RequestMethod.PUT)
    public void reviewById(@RequestParam int id,
                           @RequestParam(required = false,defaultValue = "") String description,
                           @RequestParam int type) throws IllegalQuestionException {
        questionScatteredService.review(id,description,type);
    }

    /**
     * 查找拒绝信息
     * @param id
     * @return
     */
    @RequestMapping(value = "common/findRefuseInfo", method = RequestMethod.GET)
    public Object findRefuseInfo(@RequestParam int id){
        return questionScatteredService.findRefuseInfo(id);
    }


    @RequestMapping(value = "common/del", method = RequestMethod.PUT)
    public void del(@RequestParam int id,
                    @RequestParam int status,HttpServletRequest request) throws BizException, IOException, BadElementException {
        final HttpSession session = request.getSession(true);
        User user = (User) session.getAttribute("user");
        String account = "";
        int uid = 2;
        if(user!=null){
            account = user.getAccount();
            uid = user.getId();
        }
        questionScatteredService.del(id,status,account,uid);
    }
}
