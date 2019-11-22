package com.huatu.tiku.essay.web.controller.api.test;

import com.huatu.common.exception.BizException;
import com.huatu.common.spring.web.MediaType;

import com.huatu.tiku.essay.constant.status.SystemConstant;
import com.huatu.tiku.essay.entity.EssayQuestionAnswer;
import com.huatu.tiku.essay.repository.EssayQuestionAnswerRepository;
import com.huatu.tiku.essay.service.EssayTestService;
import com.huatu.tiku.essay.service.UserCorrectGoodsService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.vo.resp.EssayMockVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Created by x6 on 2018/4/5.
 */
@RestController
@RequestMapping("api/v1/test")
@Slf4j
public class ApiTestController {
    @Autowired
    EssayQuestionAnswerRepository essayQuestionAnswerRepository;
    @Autowired
    EssayTestService essayTestService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    UserCorrectGoodsService userCorrectGoodsService;
    /**
     * 测试批量插入（是否会返回id）
     */
    @LogPrint
    @PostMapping(value="batch")
    public Object batch() throws BizException {
        long current = System.currentTimeMillis() / 1000;
//        essayTestService.batchInsert();
        //测试自定义返回类型
//        List<PaperAnswerStatisVO> statisDataTest = essayQuestionAnswerRepository.findStatisData(695L,0);
//        mockRedisKey=m_e_a_1228_765_233906505, answerCardId=2532, examType=0, userId=0, terminal=0, paperId=0
        EssayMockVO essayMockVO = EssayMockVO.builder()
                .answerCardId(2532)
                .mockRedisKey("m_e_a_1228_765_233906505")
                .build();

        log.info("=====进入批改试卷接口【模考】，发送消息到消息队列:" + essayMockVO + "=====");
        rabbitTemplate.convertAndSend(SystemConstant.MOCK_ANSWER_CORRECT_ROUTING_KEY, essayMockVO);

        return "测试批量插入结束";
    }



    @LogPrint
    @PostMapping(value="file")
    public Object file(MultipartFile file)  {

        return "测试图片上传结束";
    }



    /**
     * 批量导入数据（购买课程赠送42次批改）
     */
    @LogPrint
    @PostMapping(value = "import", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object importCourse() {
        return userCorrectGoodsService.importCourse();

    }

    /**
     * 测试订单关闭service
     */
    @LogPrint
    @PostMapping(value = "close", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object orderClose() {
         userCorrectGoodsService.closeOrder();
        return "测试订单关闭";

    }








    @LogPrint
    @PostMapping(value="answer/{id}")
    public Object answer(@PathVariable Long id)  {
        EssayQuestionAnswer one =  essayQuestionAnswerRepository.findOne(id);

        return one;
    }

    @LogPrint
    @GetMapping(value="answer/export")
    public Object answer(@RequestParam long id,
                         @RequestParam double examScoreMax,
                         @RequestParam double examScoreMin)  {
        List<EssayQuestionAnswer> exportAnswerList = essayQuestionAnswerRepository.findByExportCondition(id, examScoreMin, examScoreMax);
        LinkedList<String> content = new LinkedList<>();
        if(CollectionUtils.isNotEmpty(exportAnswerList)){
            exportAnswerList.forEach(answer->{
                content.add(answer.getContent());
            });
        }
        return content;
    }


    @LogPrint
    @PostMapping(value="formula")
    public Object file( )  {
        essayTestService.formula();
        return "测试图片上传结束";
    }

}
