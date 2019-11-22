package com.huatu.ztk.knowledge.common.analysis.config;

import com.huatu.ztk.knowledge.common.analysis.listener.ContextClosedListener;
import com.huatu.ztk.knowledge.controller.InitUserAnswersController;
import com.sensorsdata.analytics.javasdk.SensorsAnalytics;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhengyi
 * @date 2018-12-18 11:11
 **/
@Configuration
@Slf4j
public class AnalysisConfig {
    private static final Logger logger = LoggerFactory.getLogger(AnalysisConfig.class);

    @Autowired
    private SaProperties getSaUrl;

    private static final boolean SA_WRITE_DATA = true;

    @Bean
    public SensorsAnalytics sensorsAnalytics() {
        logger.info("sa path:{}", getSaUrl);
        SensorsAnalieseTicsProxy sensorsAnalieseTicsProxy = new SensorsAnalieseTicsProxy(getSaUrl.getSaUrl(), SA_WRITE_DATA);
        new ContextClosedListener(sensorsAnalieseTicsProxy.initializeBean());
        return sensorsAnalieseTicsProxy.initializeBean();
    }

}