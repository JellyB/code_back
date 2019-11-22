package com.huatu.ztk.question.controller;

import com.google.common.primitives.Ints;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.service.NetSchoolQuestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * 网校试题控制层
 * Created by linkang on 8/29/16.
 */
@RestController
@RequestMapping(value = "/v1/questions/netschool",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class NetSchoolQuestionController {

    private final static Logger logger = LoggerFactory.getLogger(NetSchoolQuestionController.class);
    @Autowired
    private NetSchoolQuestionService netSchoolQuestionService;


//    @Deprecated
//    @RequestMapping(value = "")
//    public Object findBath(@RequestParam("ids")String ids) throws Exception{
//        if (StringUtils.isEmpty(ids)) {
//            return new ArrayList();
//        }
//        final List questions = netSchoolQuestionService.findBath(getIdList(ids));
//        return questions;
//    }

    /**
     *分页查询模块下的试题
     *
     * @param size
     * @param page
     * @param modules
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "",params = "modules")
    public Object findQuestions(@RequestParam(defaultValue = "20") int size,
                                @RequestParam(defaultValue = "1") int page,
                                HttpServletRequest httpServletRequest,
                                @RequestParam String modules) throws Exception{
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}",url,remoteAddr);
        if (StringUtils.isEmpty(modules)) {
            return new ArrayList();
        }
        page = Math.max(1, page);
        List<Integer> moduleIds = getIdList(modules);
        List<Question> questions = netSchoolQuestionService.findQuestions(moduleIds, size, page);
        return questions;
    }

    @RequestMapping(value = "",params = "ids")
    public Object findBath(@RequestParam("ids")String ids,HttpServletRequest httpServletRequest) throws Exception{
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}",url,remoteAddr);
        if (StringUtils.isEmpty(ids)) {
            return new ArrayList();
        }
        List<Integer> idList = new ArrayList();
        for (String str : ids.split(",")) {
            Integer id = Ints.tryParse(str);
            if (id == null) {//id列表转换错误
                return CommonErrors.INVALID_ARGUMENTS;
            }
            idList.add(id);
        }
        final List questions = netSchoolQuestionService.findBath(idList);
        return questions;
    }

    private List<Integer> getIdList(String ids) throws BizException{
        List<Integer> idList = new ArrayList();
        for (String str : ids.split(",")) {
            Integer id = Ints.tryParse(str);
            if (id == null) {//id列表转换错误
                throw new BizException(CommonErrors.INVALID_ARGUMENTS);
            }
            idList.add(id);
        }
        return idList;
    }
}
