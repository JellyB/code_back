package com.huatu.tiku.teacher.controller.systems;

import com.huatu.tiku.teacher.service.systems.QuestionPointService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sy/point")
@Slf4j
public class QuestionPointController {

    @Autowired
    private QuestionPointService questionPointService;

    /**
     *  查询所有绑定试题的知识点绑题数量
     * @return
     */
    @GetMapping("count")
    public Object getPointQuestionCount(){
        return questionPointService.getPointQuestionCount();
    }

    /**
     *  查询所有绑定试题的知识点绑题数量
     * @return
     */
    @GetMapping("list")
    public Object getPointQuestionIds(){
        return questionPointService.getPointQuestionIds();
    }
}
