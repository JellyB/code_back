package com.huatu.tiku.teacher.controller.admin.util;

import com.huatu.common.SuccessMessage;
import com.huatu.tiku.entity.question.BaseQuestion;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.teacher.dao.question.BaseQuestionMapper;
import com.huatu.tiku.teacher.service.common.ImportService;
import com.huatu.tiku.teacher.service.question.v1.CommonQuestionServiceV1;
import com.huatu.ztk.question.bean.Question;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据迁移（mysql ==》mongo）
 * Created by x6 on 2018/6/25.
 */
@RestController
@RequestMapping("/backend/import/mysql2mongo")
public class ImportController {

    private static final Logger logger = LoggerFactory.getLogger(ImportController.class);

    @Autowired
    private ImportService importService;

    @Autowired
    CommonQuestionServiceV1 teacherQuestionService;

    @Autowired
    BaseQuestionMapper baseQuestionMapper;

    @Autowired
    CommonQuestionServiceV1 commonQuestionServiceV1;

    /**
     * 将mysql数据迁移到mongo
     * （暂时暴露成接口，在试卷审核or上线时直接调用即可）
     */
    @PostMapping(value = "question/{questionId}")
    public Object importQuestion(@PathVariable long questionId) {
        //试题从mysql导入到mongo
        importService.importQuestion(questionId);
        return SuccessMessage.create();
    }


    /**
     * 将mysql数据迁移到mongo
     * （暂时暴露成接口，在试卷审核or上线时直接调用即可）
     */
    @PostMapping(value = "paper/{paperId}")
    public Object importPaper(@PathVariable long paperId) {

        //试卷从mysql导入到mongo
        importService.importPaper(paperId);
        return SuccessMessage.create();
    }

    /**
     * 批量同步试题到mongo(暴漏为接口,方便处理问题使用)
     *
     * @param questionIds
     * @return
     */
    @PostMapping(value = "syncQuestionsByBatch")
    public Object sendQuestion2MongoBatch(@RequestParam String questionIds) {
        List<Integer> questionList = Arrays.stream(questionIds.split(","))
                .map(Integer::valueOf)
                .collect(Collectors.toList());
        questionList.stream().forEach(questionId -> {
            BaseQuestion question = baseQuestionMapper.selectByPrimaryKey(Long.valueOf(questionId));
            if (null != question) {
                importService.sendQuestion2Mongo(questionId);
            } else {
                logger.info("试题不存在:{}", questionId);
            }
        });
        return SuccessMessage.create();
    }


    /**
     * 查重功能,将mysql试题批量同步到Es中
     */
    @GetMapping(value = "sendQuestionToEsForDuplicate")
    public void sendQuestionToEsForDuplicate(@RequestParam(defaultValue = "-1") int subjectId) {
        commonQuestionServiceV1.findAndHandlerQuestion(questions -> {
                    for (Question question : questions) {
                        int id = question.getId();
                        importService.sendQuestion2SearchForDuplicate(new Long(id));
                        System.out.println("发送ES 试题ID是：{}" + id);
                    }
                }, subjectId
        );
//        List<BaseQuestion> questions = teacherQuestionService.selectAll();
//        //List<Long> questionList = questions.stream().map(baseQuestion -> baseQuestion.getId()).collect(Collectors.toList());
//        List<Long> question = new ArrayList<>();
//        question.add(30005769L);
//        question.stream().limit(1000).
//                forEach(id -> {
//                    importService.sendQuestion2SearchForDuplicate(id);
//                    System.out.println("发送ES 试题ID是：{}" + id);
//                });
    }


    /**
     * 以试卷为单位，mysql同步试题到mongo中
     * isUpdateBizStatus 表识是否修改起状态
     * type 试卷类型,默认是试题卷
     */
    @GetMapping(value = "sendQuestion2MongoByPaper")
    public Object sendQuestion2MongoByPaper(@RequestParam(defaultValue = "false") Boolean isUpdateBizStatus,
                                            @RequestParam Long paperId,
                                            @RequestParam(defaultValue = "1") Integer type) {
        PaperInfoEnum.TypeInfo typeInfo = PaperInfoEnum.TypeInfo.create(type);
        importService.sendQuestion2MongoByPaper(isUpdateBizStatus, paperId, typeInfo);
        return SuccessMessage.create("同步成功!");
    }


    /**
     * 根据知识点同步试题到mongo
     */
    @GetMapping(value = "sendQuestion2MongoByKnowledge")
    public Object sendQuestion2MongoByKnowledge() {
        importService.sendQuestion2MongoByKnowledge();
        return SuccessMessage.create("同步成功!");
    }

    /**
     * 根据知识点同步试题到mongo
     */
    @GetMapping(value = "source")
    public Object importQuestionSource(@RequestParam int subject) {
        importService.importQuestionSource(subject);
        return SuccessMessage.create("同步成功!");
    }

}
