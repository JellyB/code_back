package com.huatu.ztk.knowledge.controller;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.huatu.ztk.commons.SubjectType;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.knowledge.api.QuestionStrategyDubboService;
import com.huatu.ztk.knowledge.bean.QuestionStrategy;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 *
 * 第三方接口
 * Created by shaojieyue
 * Created time 2016-10-25 16:52
 */

@RestController
@RequestMapping(value = "/v1/third/", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class QuestionThirdController {
    private static final Logger logger = LoggerFactory.getLogger(QuestionThirdController.class);

    @Autowired
    private QuestionStrategyDubboService questionStrategyDubboService;

    //小于0，随机选取知识点
    private static final int RANDOM_POINT_ID = -1;

    /**
     * 随机抽取试题
     * @return
     */
    @RequestMapping(value = "questions/extract",method = RequestMethod.GET)
    public Object extract(@RequestParam(defaultValue = "10") int size, HttpServletRequest httpServletRequest) throws BizException{
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}",url,remoteAddr);
        return questionStrategyDubboService.randomStrategyNoUser(SubjectType.GWY_XINGCE, RANDOM_POINT_ID, size);
    }

    /**
     * 支持多个知识点进行抽题
     * @param difficult
     * @param points 规则： 知识点id1:抽题数量1,知识点id2:抽题数量2,...
     * @return
     */
    @RequestMapping(value = "questions/multi",method = RequestMethod.GET)
    public Object multiple(@RequestParam(defaultValue = "-1") int difficult,
                           HttpServletRequest httpServletRequest,
                           @RequestParam String points){
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}",url,remoteAddr);
        // TODO: 2/8/17 解析参数，分多次调用 randomStrategyNoUser，最后合并返回结果
        List<QuestionStrategy> list = Lists.newArrayList();
        String[] pointArr =  StringUtils.split(points,",");
        if(pointArr!=null&&pointArr.length>0){
            for (String point:pointArr  ) {
                String[] randomPointArr =  StringUtils.split(point,":");
                if(randomPointArr!=null&&randomPointArr.length>0){
                    QuestionStrategy questionStrategy =questionStrategyDubboService.randomStrategyNoUser(SubjectType.GWY_XINGCE, Ints.tryParse(randomPointArr[0]), Ints.tryParse(randomPointArr[1]));
                    if(questionStrategy!=null){
                        list.add(questionStrategy);
                    }
                }
            }
        }
//        return questionStrategyDubboService.randomStrategyNoUser(SubjectType.GWY_XINGCE, RANDOM_POINT_ID, size);
        final QuestionStrategy strategy = QuestionStrategy.builder()
                .difficulty(3)
                .questions(Lists.newArrayList())
                .modules(Lists.newArrayList())
                .build();

        //合并试题集合，把多个知识点抽出的题，合并为一个
        for (QuestionStrategy questionStrategy : list) {
            strategy.getQuestions().addAll(questionStrategy.getQuestions());
            strategy.getModules().add(questionStrategy.getModules().get(0));
        }
        return strategy;
    }

}
