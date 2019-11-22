package com.huatu.ztk.backend.system.controller;

import com.huatu.ztk.backend.feedback.feedback.Feedback;
import com.huatu.ztk.backend.system.bean.NsTextMsg;
import com.huatu.ztk.backend.system.service.SystemService;
import com.huatu.ztk.backend.user.bean.User;
import com.huatu.ztk.commons.PageBean;
import com.huatu.ztk.commons.exception.SuccessMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ht on 2016/11/21.
 */
@RestController
@RequestMapping("/system")
public class SystemController {

    private static final Logger logger = LoggerFactory.getLogger(SystemController.class);

    @Autowired
    private SystemService systemService;

    /**
     * 消息列表
     * @return
     */
    @RequestMapping(value = "messages",method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object list(@RequestParam(required = false) String title,
                       @RequestParam(required = false) int catgory){
        NsTextMsg textMsg=NsTextMsg.builder().title(title).catgory(catgory).build();
        List<NsTextMsg> msgList=systemService.query(textMsg);
        PageBean pageBean = new PageBean(msgList,msgList.size(),400);
        return pageBean;
    }

    /**
     *删除消息
     * @param id
     * @param request
     * @return
     */
    @RequestMapping(value = "messages",method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object delete(@RequestParam int id, HttpServletRequest request){
       boolean result= systemService.delete(id);
        return result;
    }

    /**
     * 新增保存系统消息
     * @param msg
     * @return
     */
    @RequestMapping(value = "messages",method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object insert(@RequestBody NsTextMsg msg){
        int  result= systemService.insert(msg);
       return SuccessMessage.create("保存成功");
    }

    /**
     * 获取系统消息
     * @param id
     * @return
     */
    @RequestMapping(value = "messages/{id}",method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object find(@PathVariable int id){
        NsTextMsg nsTextMsg=systemService.findById(id);
        return nsTextMsg;
    }

    /**
     * 修改保存系统消息
     * @param msg
     * @return
     */
    @RequestMapping(value = "messages",method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object update(@RequestBody NsTextMsg msg){
        int  result= systemService.update(msg);
        return SuccessMessage.create("修改保存成功");
    }
}
