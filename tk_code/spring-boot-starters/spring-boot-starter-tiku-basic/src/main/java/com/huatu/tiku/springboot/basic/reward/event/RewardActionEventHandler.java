package com.huatu.tiku.springboot.basic.reward.event;

import com.huatu.tiku.common.bean.reward.RewardResult;
import com.huatu.tiku.springboot.basic.reward.RewardAction;
import org.springframework.core.Ordered;

/**
 * @author hanchao
 * @date 2017/10/13 14:19
 */
public interface RewardActionEventHandler extends Ordered {
    /**
     * 是否可以处理
     * @return
     */
    boolean canHandle(RewardAction.ActionType actionType);

    /**
     * 处理
     */
    RewardResult handle(RewardActionEvent actionEvent);
}
