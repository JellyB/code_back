package com.huatu.ztk.question.controller;

import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import com.huatu.ztk.commons.TerminalType;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.bean.QuestionMeta;
import com.huatu.ztk.question.service.QuestionRecordService;
import com.huatu.ztk.question.service.QuestionService;
import com.huatu.ztk.user.service.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 试题控制层
 * Created by shaojieyue on 4/16/16.
 */
@CrossOrigin
@RestController
@RequestMapping(value = "/v1/questions", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class QuestionController {
    private static final Logger logger = LoggerFactory.getLogger(QuestionController.class);

    @Autowired
    private QuestionRecordService questionRecordService;

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private QuestionService questionService;

    /**
     * 获取单个问题实体
     *
     * @return
     */
    @RequestMapping(value = "{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object get(@RequestHeader(required = false) String cv, @PathVariable("id") int id,
                      @RequestHeader(required = false, defaultValue = "-1") Integer terminal) {
//        final Question question = questionService.findById(id, cv);
        final Question question = questionService.findByIdWithTerminal(id, cv,terminal);
        if (question == null) {//试题不存在
            return CommonErrors.RESOURCE_NOT_FOUND;
        }
        return question;
    }

    @RequestMapping(value = "getAllQuestionByType/{type}/{mode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object getAllQuestionMetaByType(
            @PathVariable("type") Integer type,
            @PathVariable("mode") Integer mode,
            HttpServletRequest httpServletRequest
    ) {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}", url, remoteAddr);
        List<Question> questionList = questionService.findByType(type, mode);
        return questionList;
    }

    @RequestMapping(value = "getAllQuestionByTypeForSimple/{type}/{mode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object getAllQuestionByTypeForSimple(
            @PathVariable("type") Integer type,
            @PathVariable("mode") Integer mode
    ) {
        List<Question> questionList = questionService.findByType(type, mode);
        List<HashMap<String, Object>> collect = questionList.stream()
                .filter(Objects::nonNull)
                .map(question -> {
                    GenericQuestion genericQuestion = (GenericQuestion) question;
                    HashMap<String, Object> map = Maps.newHashMap();
                    map.put("id", genericQuestion.getId());
                    //map.put("material", genericQuestion.getMaterial());
                    map.put("stem", genericQuestion.getStem());
                    map.put("choice", genericQuestion.getChoices().stream()
                            .collect(Collectors.joining("")));
                    map.put("pointName", genericQuestion.getPointsName().stream()
                            .collect(Collectors.joining("--")));
                    QuestionMeta meta = genericQuestion.getMeta();
                    map.put("count", meta == null ? "" : meta.getCount());
                    map.put("percent", meta == null ? "" : meta.getPercents()[meta.getRindex()]);
                    return map;
                })
                .collect(Collectors.toList());
        return collect;
    }


    /**
     * 批量获取问题实体
     *
     * @param cv  版本号
     * @param ids id列表,逗号分割
     * @return
     */
    @CrossOrigin(origins = "*", allowedHeaders = "terminal,token")
    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object bath(@RequestHeader(required = false) String cv, @RequestParam("ids") String ids,
                       @RequestHeader(required = false, defaultValue = "-1") Integer terminal) throws BizException {
        long start = System.currentTimeMillis();
        if (StringUtils.isEmpty(ids)) {
            return new ArrayList();
        }

        String[] idArray = ids.split(",");
        List idList = new ArrayList();
        for (String str : idArray) {
            Integer id = Ints.tryParse(str);
            if (id == null) {//id列表转换错误
                throw new BizException(CommonErrors.INVALID_ARGUMENTS);
            }
            idList.add(id);
        }
//        final List<Question> questions = questionService.findBath(idList, cv);
        final List<Question> questions = questionService.findBathWithTerminal(idList, cv,terminal);
        if (terminal == TerminalType.IPHONE || terminal == TerminalType.IPHONE_IPAD) {
            //logger.info("ios需单独处理特殊标签");
            questionService.convertSpecialTag(questions);
        }
        logger.info("zhouwei31："+(System.currentTimeMillis()-start));
        return questions;
    }

    @RequestMapping(value = "/myrecords", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object myrecords(@RequestParam("qids") String qids, @RequestHeader(required = false) String token) throws BizException {
        userSessionService.assertSession(token);
        final long uid = userSessionService.getUid(token);

        if (StringUtils.isEmpty(qids)) {
            return new ArrayList();
        }
        final int[] questionIds = Arrays.stream(qids.split(",")).mapToInt(Integer::valueOf).toArray();
        return questionRecordService.findBatch(uid, questionIds);
    }
}
