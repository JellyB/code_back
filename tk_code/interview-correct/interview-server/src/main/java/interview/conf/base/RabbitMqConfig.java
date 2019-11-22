package interview.conf.base;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
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

    @Bean("rabbitFactory")
    public SimpleRabbitListenerContainerFactory pointTaskContainerFactory(SimpleRabbitListenerContainerFactoryConfigurer configurer, ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setPrefetchCount(60);
        factory.setConcurrentConsumers(20);
        configurer.configure(factory, connectionFactory);
        return factory;
    }

    @Bean
    public Queue abcdQueue(){
        return new Queue("abcd",false, false, false,null);
    }

}
