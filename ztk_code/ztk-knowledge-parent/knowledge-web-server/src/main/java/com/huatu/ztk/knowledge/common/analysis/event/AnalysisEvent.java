package com.huatu.ztk.knowledge.common.analysis.event;

import com.huatu.ztk.knowledge.common.analysis.model.EventEntity;
import org.springframework.context.ApplicationEvent;

/**
 * @author zhengyi
 * @date 2018-12-21 14:02
 **/
public class AnalysisEvent extends ApplicationEvent {


    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public AnalysisEvent(EventEntity source) {
        super(source);
    }
}