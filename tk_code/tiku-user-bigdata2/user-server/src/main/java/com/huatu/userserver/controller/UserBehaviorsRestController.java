package com.huatu.userserver.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class
UserBehaviorsRestController {

    @GetMapping("/a/v1/function/analysis")
    @ResponseBody
    public Object userFunAnalysis(@RequestParam(value = "name", required = false) String name
            , @RequestParam(value = "fun_id", required = false) String fun_id) {
        System.out.println(name + "" + fun_id);
        return 1;
    }

    @GetMapping("/a/v1/topic/record")
    @ResponseBody
    public Object topicRecord(@RequestParam(value = "question_id", required = false) Long question_id //试题Id
            , @RequestParam(value = "user_id", required = false) Long user_id //用户id
            , @RequestParam(value = "time", required = false) Long time //做题时长
            , @RequestParam(value = "correct", required = false) Integer correct //是否正确
            , @RequestParam(value = "knowledge_point", required = false) Integer knowledge_point //所属知识点
            , @RequestParam(value = "question_source", required = false) Integer question_source //视频来源（课中题，课后题）
            , @RequestParam(value = "course_ware_id", required = false) Long course_ware_id//课件id
            , @RequestParam(value = "submit_time", required = false) Long submit_time//提交时间
    ) {
        System.out.println(question_id + "" + user_id + "" + time + "" + correct + "" + knowledge_point + "" + question_source + "" + course_ware_id + "" + submit_time);
        return 1;
    }
}
