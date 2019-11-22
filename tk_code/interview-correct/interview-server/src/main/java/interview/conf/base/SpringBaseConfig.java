package interview.conf.base;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.huatu.common.spring.conventer.FormMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * @author hanchao
 * @date 2017/8/18 15:47
 */
@Configuration
@EnableApolloConfig
@EnableAsync
@EnableScheduling
public class SpringBaseConfig {

    /**
     * 支持map转url encode，可以用于post body而不是放在url上
     * feign decoder默认使用了全局的httpmessageconverters
     * @return
     */
    @Bean
    public FormMessageConverter formMessageConverter(){
        return new FormMessageConverter();
    }

}
