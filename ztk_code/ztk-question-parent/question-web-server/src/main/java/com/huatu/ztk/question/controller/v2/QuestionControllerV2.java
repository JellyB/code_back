package com.huatu.ztk.question.controller.v2;

import com.google.common.primitives.Ints;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


/**
 * @author zhouwei
 * 获取试题接口 不传用户信息 将接口推向CDN
 */

@RestController
@RequestMapping(value = "/v2/questions", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class QuestionControllerV2 {
    @Autowired
    private QuestionService questionService;


    /**
     * 批量获取问题实体
     *
     * @param ids id列表,逗号分割
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseBody
    public Object bath(@RequestParam("ids") String ids,
                       @RequestHeader(required = false, defaultValue = "-1") Integer terminal) throws BizException {
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
//        final List<Question> questions = questionService.findBath(idList,null);
        final List<Question> questions = questionService.findBathWithTerminal(idList,null,terminal);
        return questions;
    }


}
