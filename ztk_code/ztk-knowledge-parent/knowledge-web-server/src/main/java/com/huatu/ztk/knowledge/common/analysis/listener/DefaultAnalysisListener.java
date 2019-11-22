package com.huatu.ztk.knowledge.common.analysis.listener;

import com.huatu.ztk.knowledge.common.analysis.event.AnalysisEvent;
import com.sensorsdata.analytics.javasdk.SensorsAnalytics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zhengyi
 * @date 2018-12-22 21:27
 **/
@Component
public class DefaultAnalysisListener implements AnalysisListener {
    private final SensorsAnalytics sensorsAnalytics;

    @Autowired
    public DefaultAnalysisListener(SensorsAnalytics sensorsAnalytics) {
        this.sensorsAnalytics = sensorsAnalytics;
    }

    @Override
    public void onApplicationEvent(AnalysisEvent event) {
//        EventEntity source = (EventEntity) event.getSource();
//        try {
//            sensorsAnalytics.track(source.getDistinctId(), true, source.getEventName(), source.getPropertiesMap());
//        } catch (InvalidArgumentException e) {
//            e.printStackTrace();
//        }
    }
}