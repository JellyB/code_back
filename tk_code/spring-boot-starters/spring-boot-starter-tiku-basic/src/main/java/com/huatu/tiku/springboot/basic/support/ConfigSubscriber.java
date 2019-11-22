package com.huatu.tiku.springboot.basic.support;

import com.ctrip.framework.apollo.model.ConfigChange;

/**
 * 需要监听变化额外处理才用这个，如果只是最新值，可以通过config.get或者RefreshScope来实现
 * @author hanchao
 * @date 2017/10/6 14:10
 */
public interface ConfigSubscriber {
    void update(ConfigChange configChange);
    String key();

    /**
     * 默认namespace使用ConfigConsts.NAMESPACE_APPLICATION或者直接返回""
     * @return
     */
    String namespace();

    /**
     * 是否完成后通知，方便初始化
     * @return
     */
    default boolean notifyOnReady(){
        return false;
    }
}
