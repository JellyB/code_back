package com.huatu.ztk.backend.feedback.controller;

import com.huatu.ztk.backend.feedback.feedback.Feedback;
import com.huatu.ztk.backend.feedback.service.FeedBackService;
import com.huatu.ztk.commons.PageBean;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shaojieyue
 * Created time 2016-11-14 16:40
 */

@RestController
@RequestMapping("/feedbacks")
public class FeedbackController {
    private static final Logger logger = LoggerFactory.getLogger(FeedbackController.class);

    @Autowired
    private FeedBackService feedBackService;

    /**
     * 用户反馈
     * @param catgory  科目
     * @return
     */
    @RequestMapping(value = "",method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object list(@RequestParam(defaultValue = "0")int catgory){
        return feedBackService.query(catgory);
    }


    /**
     * 反馈详情
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}",method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object getFeedBack(@PathVariable int id){
        Feedback feedback=feedBackService.find(id);
        return  feedback;
    }
}
