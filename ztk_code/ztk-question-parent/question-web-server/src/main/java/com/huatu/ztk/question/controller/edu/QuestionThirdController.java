package com.huatu.ztk.question.controller.edu;

import com.google.common.primitives.Ints;
import com.huatu.ztk.commons.TerminalType;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.service.QuestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/v1/edu/questions", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class QuestionThirdController {
    private static final Logger logger = LoggerFactory.getLogger(QuestionThirdController.class);

    @Autowired
    private QuestionService questionService;

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
                       @RequestHeader(required = false, defaultValue = "-1") Integer terminal,
                       @RequestHeader(defaultValue = "-1") int subject) throws BizException {
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
}
