package com.huatu.ztk.backend.question.controller;

import com.huatu.ztk.backend.question.dao.QuestionDao;
import com.huatu.ztk.backend.question.service.QuestionService;
import com.huatu.ztk.backend.question.service.QuestionServiceV1;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.question.bean.Question;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by huangqp on 2018\6\8 0008.
 */
@RestController
@RequestMapping("question/v1")
public class QuestionControllerV1 {
    private static final Logger logger = LoggerFactory.getLogger(QuestionControllerV1.class);
    @Autowired
    QuestionServiceV1 questionServiceV1;
    @Autowired
    QuestionService questionService;
    @Autowired
    QuestionDao questionDao;

    /**
     * 散题导出（散题下载）
     *
     * @param subject
     * @param pointId
     * @param isReNew
     * @return
     */
    @RequestMapping(value = "/file", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object createQuestionFile(@RequestParam Integer subject, @RequestParam Integer pointId, @RequestParam Integer isReNew) {
        return questionServiceV1.createQuestionFile(subject, pointId, isReNew);
    }

    /**
     * 散题导出（散题下载）
     * 每个模块200道题
     *
     * @return
     */
    @RequestMapping(value = "/file/{moduleId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object createQuestionFile1(@PathVariable Integer moduleId) {

        return questionServiceV1.createQuestionFileByModule(moduleId);
    }

    /**
     * 删除某一个用户的收藏试题ID
     * @param userId
     * @param questionId
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "/collect", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object deleteCollect(@RequestParam Long userId, @RequestParam Integer questionId) throws BizException {
        return questionServiceV1.deleteUserCollect(userId, questionId);
    }

    /**
     * 以试卷为单位，调用迁移试卷详情接口，同步其中可以同步的所有试题（迁移预处理）
     *
     * @param
     */
    @RequestMapping(value = "/sync", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object deleteCollect(@RequestParam String subjects) throws BizException {
        String[] split = subjects.split(",");

        for (String s : split) {
            questionServiceV1.syncQuestionsBySubject(Integer.parseInt(s));
        }
        return SuccessMessage.create("更新任务已提交");
    }

    /**
     * 清除缓存（支持多题批量处理）
     * @param ids
     */
    @RequestMapping(value = "/cache", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void clearQuestionCache(@RequestParam String ids){
        List<Integer> list = Arrays.stream(ids.replace("，", ",").split(","))
                .map(Integer::new).collect(Collectors.toList());
        for (int id : list) {
            Question question = questionDao.findAllTypeById(id);
            if(null != question){
                questionService.updateQuestion(question,-1);
            }
        }
    }
}

