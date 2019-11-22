package com.huatu.tiku.galaxy.spring.conf.base;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huatu.tiku.essay.constant.SystemConstant;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.huatu.common.consts.ApolloConfigConsts.NAMESPACE_TIKU_RABBIT;

/**
 * @author hanchao
 * @date 2017/9/4 14:05
 */
@EnableApolloConfig(NAMESPACE_TIKU_RABBIT)
@Configuration
public class RabbitMqConfig {
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(@Autowired ObjectMapper objectMapper){
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public Queue abcdQueue(){
        return new Queue("abcd",false, false, false,null);
    }

//    /**
//     * 试卷批改 MQ队列
//     * @return
//     */
//    @Bean
//    public Queue correctQueue(){
//        return new Queue(SystemConstant.ANSWER_CORRECT_ROUTING_KEY,false, false, false,null);
//    }


//    /**
//     * 试卷批改 MQ队列
//     * 答案信息持久化
//     * @return
//     */
//    @Bean
//    public Queue durableAnswerQueue(){
//        return new Queue(SystemConstant.ANSWER_DURABLE_ROUTING_KEY,false, false, false,null);
//    }
    /**
     * 生成PDF文件
     * @return
     */
    @Bean
    public Queue createPdfQueue(){
        return new Queue(SystemConstant.CREATE_PDF_ROUTING_KEY,false, false, false,null);
    }


    /**
     * 试题关键句智能获取分词队列
     * @return
     */
    @Bean
    public Queue keyPhraseQueue(){
        return new Queue(SystemConstant.ESSAY_STANDARD_ANSWER_KEY_PHRASE_QUEUE,false, false, false,null);
    }

    /**
     * 为用户创建答题卡id
     * @return
     */
    @Bean
    public Queue createAnswerCard(){
        return new Queue(SystemConstant.CREATE_ESSAY_MOCK_ANSWER_CARD_QUEUE,false, false, false,null);
    }


    /**
     * 批改完成 批改详情存入缓存  成绩存入Zset（4个）得分 总分统计（平均分）
     * @return
     */
    @Bean
    public Queue correctFinished(){
        return new Queue(SystemConstant.ANSWER_CORRECT_FINISH_QUEUE,false, false, false,null);
    }



    /**
     * 试卷批改 MQ队列
     * @return
     */
    @Bean
    public Queue mockCorrectQueue(){
        return new Queue(SystemConstant.MOCK_ANSWER_CORRECT_ROUTING_KEY,false, false, false,null);
    }







    /*@Bean
    public Exchange abcdExchange(){
        return new FanoutExchange("abcdex");
    }

    @Bean
    public Binding testbingding(@Autowired @Qualifier("abcdQueue")Queue queue,
                                @Autowired @Qualifier("abcdExchange")Exchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with("abcd-route").noargs();
}*/
}
