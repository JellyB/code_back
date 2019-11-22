package com.huatu.tiku.springboot.basic.support;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * 监听配置中心的配置变化
 * @author hanchao
 * @date 2017/10/6 14:00
 */
public class ConfigListenerAdapter implements InitializingBean {

    @Autowired
    private List<ConfigSubscriber> configSubscriberList;


    @Override
    public void afterPropertiesSet() throws Exception {
        ImmutableTable.Builder<String,String,ConfigSubscriber> tableBuilder = ImmutableTable.builder();
        if(CollectionUtils.isEmpty(configSubscriberList)){
            return;
        }
        for (ConfigSubscriber configSubscriber : configSubscriberList) {
            tableBuilder.put(configSubscriber.namespace(),configSubscriber.key(),configSubscriber);
        }

        Table<String,String,ConfigSubscriber> subscribers = tableBuilder.build();

        //依次增加监听,使用java base方式
        for (String namespace : subscribers.rowKeySet()) {
            Config config = StringUtils.isBlank(namespace) ? ConfigService.getAppConfig() : ConfigService.getConfig(namespace);
            config.addChangeListener(changeEvent -> {
                Map<String, ConfigSubscriber> keys = subscribers.row(namespace);
                for (String key : keys.keySet()) {
                    if(changeEvent.isChanged(key)){
                        keys.get(key).update(changeEvent.getChange(key));
                    }
                }
            });
            //notify after
            Map<String, ConfigSubscriber> row = subscribers.row(namespace);
            for (String key : row.keySet()) {
                ConfigSubscriber configSubscriber = row.get(key);
                if(configSubscriber.notifyOnReady()){
                    configSubscriber.update(new ConfigChange(namespace,key,null,config.getProperty(configSubscriber.key(),null),null));
                }
            }
        }
    }

}
