package com.huatu.tiku.config.tool;

import com.huatu.springboot.web.tools.advice.AdviceExcluder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author zhengyi
 * @date 2019-01-24 11:39
 * <p>
 * add you need ignore class name
 * example:
 * ignoreClasses.add(com.huatu.tiku.teacher.controller.admin.question.BaseQuestionSearchController);
 * add you need ignore class name
 * example:
 * ignoreUrls.add("/baseQuestion/list/**");
 * </p>
 * <div style="color:red">
 * NOTE:
 * this class(BaseQuestionSearchController) all request will ignore wrapper response.
 * </div>
 **/
@Configuration
public class WebToolConfig {

    private static Set<String> ignoreClasses = new HashSet(16);
    private static List<String> ignoreUrls = new ArrayList(16);

    static {
        //add you need ignore class name
        ignoreClasses.add("com.huatu.tiku.teacher.controller.admin.question.FormulaFormatController");
        ignoreUrls.add("/formula/**");
    }

    @Bean
    public AdviceExcluder adviceExcluder() {
        return new AdviceExcluder(ignoreClasses, ignoreUrls);
    }
}