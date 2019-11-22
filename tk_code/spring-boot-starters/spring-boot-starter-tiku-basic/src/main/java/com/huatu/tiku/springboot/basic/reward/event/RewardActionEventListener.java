package com.huatu.tiku.springboot.basic.reward.event;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.List;

/**
 * @author hanchao
 * @date 2017/10/13 13:59
 */
@Slf4j
public class RewardActionEventListener implements InitializingBean,ApplicationListener<RewardActionEvent> {
    @Autowired(required = false)
    private List<RewardActionEventHandler> rewardActionEventHandlers;


    /**
     * 事件处理
     * @param event
     */
    @Override
    public void onApplicationEvent(RewardActionEvent event) {
        for (RewardActionEventHandler rewardActionEventHandler : rewardActionEventHandlers) {
            if(rewardActionEventHandler.canHandle(event.getAction())){
                rewardActionEventHandler.handle(event);
                return;
            }
        }
        log.error("no handler found for event {}",event.getAction());
    }



    @Override
    public void afterPropertiesSet() throws Exception {
        if(CollectionUtils.isNotEmpty(rewardActionEventHandlers)){
            AnnotationAwareOrderComparator.sort(rewardActionEventHandlers);
        }
        //Collections.sort(rewardActionEventHandlers, Comparator.comparingInt(RewardActionEventHandler::getOrder));
    }

}
