package com.huatu.ztk.backend.paper.controller;

import com.huatu.ztk.backend.paper.service.PaperQuestionService;
import com.huatu.ztk.commons.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * Created by ht on 2016/12/21.
 */
@RestController
@RequestMapping("/paper")
public class PaperQuestionController {

    private static final Logger logger = LoggerFactory.getLogger(PaperQuestionController.class);

    @Autowired
    private PaperQuestionService paperQuestionService;


    /**
     *
     * @param id 试题id
     * @return
     */
    @RequestMapping(value = "question/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object findQuestion(@PathVariable int id) throws BizException {

        return paperQuestionService.findQuestionById(id);
    }

    @RequestMapping(value = "question/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object findQuestionList(@RequestParam int paperId) throws BizException{
        return paperQuestionService.findPaperQuestionList(paperId);
    }
}
