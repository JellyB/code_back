package com.huatu.tiku.springboot.basic.reward.event;

import com.huatu.tiku.common.bean.reward.RewardMessage;
import com.huatu.tiku.common.consts.RabbitConsts;
import com.huatu.tiku.springboot.basic.reward.RewardAction;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 注意：redistemplate在全局必须统一，如果redistemplate不是属于目前cluster的，一定要自己实现该方法替换此bean
 * 注意：rabbittemplate也必须是全局统一的消息队列，如果在项目中使用了别的消息队列，自己实现接口
 * 优先级最低的处理器，优先保证用户自定义的处理器执行逻辑
 * @author hanchao
 * @date 2017/10/13 19:38
 */
public class DefaultRewardEventHandler extends AbstractRewardActionEventHandler {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private Jackson2JsonMessageConverter jackson2JsonMessageConverter;
    @Override
    public void dealMessage(RewardMessage rewardMessage) {
        rabbitTemplate.send(RabbitConsts.QUEUE_REWARD_ACTION,jackson2JsonMessageConverter.toMessage(rewardMessage,new MessageProperties()));
    }

    /**
     * 不处理签到
     * @param rewardAction
     * @return
     */
    @Override
    protected boolean canHandle(RewardAction rewardAction) {
        return rewardAction.getAction() != null &&
                rewardAction.getAction() != RewardAction.ActionType.ATTENDANCE;
    }

    @Override
    protected RedisTemplate getRedisTemplate() {
        return redisTemplate;
    }


    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
