package com.huatu.ztk.backend.question.controller;

import com.huatu.ztk.backend.question.bean.AdviceBean;
import com.huatu.ztk.backend.question.bean.QuestionAdvice;
import com.huatu.ztk.backend.question.service.QuestionAdviceService;
import com.huatu.ztk.backend.user.bean.User;
import com.huatu.ztk.commons.exception.SuccessMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Created by ht on 2017/1/4.
 */
@RestController
@RequestMapping(value = "/advice")
public class QuestionAdviceController {


    @Autowired
    private QuestionAdviceService questionAdviceService;

    /**
     *
     * @param subject  考试科目
     * @param status   状态
     * @param moduleId  试题模块id
     * @param qArea  试题地区
     * @param qType  试题题型
     * @param handler  处理人
     * @param isMine  是否是当前用户
     * @param errorType 错误类型
     * @param orderTime  排序状态(0:提交时间，1：处理时间)
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object list(@RequestParam(required = false,defaultValue = "-1") int subject,
                       @RequestParam(required = false,defaultValue = "3") int status,
                       @RequestParam(required = false,defaultValue ="0") int moduleId,
                       @RequestParam(required = false,defaultValue ="0")String qArea,
                       @RequestParam(required = false,defaultValue ="0") int qType,
                       @RequestParam(required = false) String handler,
                       @RequestParam(required = false,defaultValue ="0") int isMine,
                       @RequestParam(required = false,defaultValue ="0") int errorType,
                       @RequestParam(required = false,defaultValue ="0") int orderTime, HttpServletRequest request){
        long userId=0;
        if(isMine>0){
            HttpSession session = request.getSession(true);
            User user = (User) session.getAttribute("user");
            userId = user != null ? user.getId() : 0;
        }

        AdviceBean advice=AdviceBean.builder().subject(subject).status(status).moduleId(moduleId)
                .qType(qType).handler(handler).isMine(userId).errorType(errorType).orderTime(orderTime).build();
        return questionAdviceService.list(advice,qArea);
    }

    /**
     * 获取试题纠错的详情
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object advice(@PathVariable int id){
        QuestionAdvice questionAdvice=questionAdviceService.findAdvice(id);
        return questionAdvice;
    }

    /**
     * 处理试题采纳
     * @param id
     * @return
     */
    @RequestMapping(value = "/dealAdoption", method = RequestMethod.PUT,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object dealAdoption(@RequestParam int id){
        questionAdviceService.dealAdoption(id);
        return SuccessMessage.create("状态修改成功");
    }
    /**
     * 处理纠错的试题状态为不采纳
     * @param id
     * @return
     */
    @RequestMapping(value = "/dealNoAdoption", method = RequestMethod.PUT,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object dealNoAdoption(@RequestParam int id,
                                 @RequestParam String reason){
        questionAdviceService.dealNoAdoption(id,reason);
        return SuccessMessage.create("状态修改成功");
    }

    /**
     * 处理纠错的试题状态为不使用
     * @param id
     * @return
     */
    @RequestMapping(value = "/dealNotUse", method = RequestMethod.PUT,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object dealNotUse(@RequestParam int id){
        questionAdviceService.dealNoUse(id);
        return SuccessMessage.create("状态修改成功");
    }

    /**
     * 处理纠错的试题状态为使用
     * @param id
     * @return
     */
    @RequestMapping(value = "/dealUse", method = RequestMethod.PUT,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object dealUse(@RequestParam int id){
        questionAdviceService.dealUse(id);
        return SuccessMessage.create("状态修改成功");
    }

    /**
     * 删除
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object delete(@PathVariable int id){
        questionAdviceService.delete(id);
        return SuccessMessage.create("删除成功");
    }

}
