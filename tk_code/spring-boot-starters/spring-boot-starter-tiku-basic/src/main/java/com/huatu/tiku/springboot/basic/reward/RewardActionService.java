package com.huatu.tiku.springboot.basic.reward;

import com.ctrip.framework.apollo.model.ConfigChange;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.huatu.tiku.springboot.basic.support.ConfigSubscriber;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.huatu.common.consts.ApolloConfigConsts.NAMESPACE_TIKU_BASIC;

/**
 * 赏金任务配置
 * @author hanchao
 * @date 2017/10/12 19:38
 */
@Slf4j
public class RewardActionService implements ConfigSubscriber {
    private static volatile String configSign = "";
    private Map<String,RewardAction> actionMapping = Maps.newHashMap();

    public RewardActionService(String config) throws IOException {
        if(StringUtils.isEmpty(config)){
            throw new IllegalArgumentException("basic info cant be empty");
        }
        load(config);
    }

    private void load(String config) throws IOException {
        log.debug("load reward actions from config...");
        //
        ObjectMapper objectMapper = new ObjectMapper();

        List<RewardAction> actionList = objectMapper.readValue(config,new TypeReference<List<RewardAction>>(){});

        Map<String, RewardAction> tempMapping = actionList.stream().collect(Collectors.toMap((action) -> action.getAction().name(), Function.identity()));

        //直接修改引用地址，避免线程安全问题
        synchronized (this){
            actionMapping = tempMapping;
            configSign = DigestUtils.md5Hex(config);
        }
    }


    /**
     * 根据action查找对应的配置
     * @param aciton
     * @return
     */
    public RewardAction get(String aciton){
        return actionMapping.get(aciton);
    }

    public Map<String,RewardAction> all(){
        return Maps.newHashMap(actionMapping);
    }

    @Override
    public void update(ConfigChange configChange) {
        log.debug(">>> reward actions changed,oldValue:{} -> newValue:{}",configChange.getOldValue(),configChange.getNewValue());
        String sign = DigestUtils.md5Hex(configChange.getNewValue());
        if(Objects.equals(sign, configSign)){
            log.debug(">>> reward actions not really changed!!!");
            return;
        }
        try {
            load(configChange.getNewValue());
        } catch (IOException e) {
            log.error("",e);
        }
    }

    @Override
    public String key() {
        return "tiku.basic.reward-actions";
    }

    @Override
    public String namespace() {
        return NAMESPACE_TIKU_BASIC;
    }
}
