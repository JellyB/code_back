package com.ht.base.analysis.listener;

import com.ht.base.analysis.event.AnalysisEvent;
import com.ht.base.analysis.model.EventEntity;
import com.sensorsdata.analytics.javasdk.SensorsAnalytics;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zhengyi
 * @date 2018-12-22 21:27
 **/
@Slf4j
public class DefaultAnalysisListener implements AnalysisListener {
    private final SensorsAnalytics sensorsAnalytics;

    public DefaultAnalysisListener(SensorsAnalytics sensorsAnalytics) {
        this.sensorsAnalytics = sensorsAnalytics;
    }

    @Override
    public void onApplicationEvent(AnalysisEvent event) {
        EventEntity source = (EventEntity) event.getSource();
		try {
			sensorsAnalytics.track(source.getDistinctId(), true, source.getEventName(), source.getPropertiesMap());
			log.info("用户:{}上报事件:{}", source.getDistinctId(), source.getEventName());

		} catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
    }
}