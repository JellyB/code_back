package com.huatu.ztk.knowledge.common.analysis.event;


import com.huatu.ztk.knowledge.common.analysis.model.EventEntity;

/**
 * @author zhengyi
 * @date 2018-12-24 14:26
 **/
public class SearchPleaseEvent extends AnalysisEvent {
    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public SearchPleaseEvent(EventEntity source) {
        super(source);
    }
}