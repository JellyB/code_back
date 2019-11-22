package com.huatu.ztk.question.controller;

import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.question.bean.QuestionNote;
import com.huatu.ztk.question.service.QuestionNoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by shaojieyue on 5/3/16.
 */

@RestController
@RequestMapping(value = "/v1/notes",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class QuestionNoteController {
    public static final Logger logger = LoggerFactory.getLogger(QuestionNoteController.class);

    @Autowired
    private QuestionNoteService questionNoteService;

    /**
     * 保存笔记
     * @param questionNote
     * @return
     */
    @RequestMapping(value = "/",method = RequestMethod.POST)
    public Object save(@RequestBody QuestionNote questionNote){
        questionNote.setId(1234);
        questionNote.setCreateTime(System.currentTimeMillis());
        try {
            questionNoteService.save(questionNote);
        } catch (BizException e) {
            logger.error("save note fail.",e);
            return e.getErrorResult();
        }
        return questionNote;
    }

    /**
     * 通过 问题id查询其对应的笔记
     * @param questions
     * @return
     */
    @RequestMapping(value = "/",method = RequestMethod.GET)
    public Object query(@RequestParam int[] questions){
        int userId = 1234;
        List<QuestionNote> list = questionNoteService.findByQuestions(userId,questions);
        Map data = new HashMap();
        for (QuestionNote questionNote : list) {
            data.put(questionNote.getQuestionId(),questionNote);
        }
        return data;
    }
}
