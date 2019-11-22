package com.huatu.tiku.teacher.controller.admin.question;

/**
 * @author zhaoxi
 * @Description: TODO
 * @date 2018/9/17下午5:33
 */
import com.huatu.tiku.teacher.service.question.QuestionCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/backend/question/check")
public class QuestionCheckController {

    @Autowired
    private QuestionCheckService questionCheckService;

    /**
     * 处理mysql，mongo试题数据
     * 求 mysql和mongo的交集和差集
     * @return
     */
    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object dealNewQuestion() {
        questionCheckService.check();

        return null;

    }

//    /**
//     * 处理mysql，mongo试题数据
//     * 求 mysql和mongo的交集和差集
//     * @return
//     */
//    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
//    public Object delNewQuestion() {
//        questionCheckService.del();
//
//        return "删除成功";
//
//    }


}
