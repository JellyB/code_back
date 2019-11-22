package com.huatu.tiku.search.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.huatu.common.test.BaseWebTest;
import com.huatu.springboot.dubbo.annotation.DubboConsumer;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.Question;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @author hanchao
 * @date 2018/1/8 15:43
 */
@Slf4j
public class DubboTest extends BaseWebTest {
    @DubboConsumer(version = "2.2")
    private QuestionDubboService questionDubboService;

    @Test
    public void testQuestion(){
        Question question = questionDubboService.findById(261110);
        log.info(JSON.toJSONString(question, SerializerFeature.PrettyFormat));
    }

}
