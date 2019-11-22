package com.huatu.tiku.match.common;

import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.stream.Collectors;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2018-10-16 下午3:12
 **/
@Component
@Slf4j
@Getter
@Setter
@ConfigurationProperties(prefix = MatchConfig.PREFIX)
public class MatchConfig extends Observable {

    public static final String MATCH_PREFIX = "tiku.matchMeta";
    public static final String PREFIX = "matchMeta";
    public static final List<String> CHANGED_KEYS = Lists.newArrayList("tags");

    /**
     * 模考大赛下一场展示延时（分钟）
     */
    private int nextMatchDelayTime;

    /**
     * 报名方式为无地区报名的科目
     */
    private String enrollNoAreaSubjects;

    /**
     * 展示多个模考的模考数最大值
     *
     * @return
     */
    private int maxMatchShowSize;

    /**
     * 申论提前进入阶段时间（分钟）
     */
    private int essayLeadTime;

    /**
     * 申论延迟查看报告时间（分钟）
     */
    private int essayDelayReportTime;

    /**
     * 模考tag
     */
    private String tags;

    /**
     * 错误回调
     */
    private String matchErrorPath;

    /**
     * 报名方式为无地区报名的科目 toList
     *
     * @return
     */
    public List<Integer> getEnrollNoAreaSubjectCollection() {
        try {
            return Arrays.stream(enrollNoAreaSubjects.split(",")).map(item -> Integer.parseInt(item)).collect(Collectors.toList());
        } catch (Exception e) {
            return Lists.newArrayList();
        }
    }

    /**
     * 判断报名方式为无地区报名的科目是否包含当前subject
     *
     * @param subject
     * @return
     */
    public boolean checkCollectionContainsCurrentSubject(int subject){
        try{
            return Arrays.stream(enrollNoAreaSubjects.split(",")).map(Integer::parseInt).anyMatch(i -> i == subject);
        }catch (Exception e){
            return false;
        }
    }

    @ApolloConfigChangeListener(value = MATCH_PREFIX)
    private void valueOnChanged(ConfigChangeEvent changeEvent) {
        changeEvent.changedKeys().forEach(key -> {
            ConfigChange change = changeEvent.getChange(key);
            log.info("Found change - key:{}, changeType:{}", change.getPropertyName(), change.getChangeType());
            log.info("oldValue : {}", change.getOldValue());
            log.info("newValue : {}", change.getNewValue());
            if (CHANGED_KEYS.contains(key)) {
                setChanged();
                notifyObservers(change.getNewValue());
            }
        });
    }

    @PostConstruct
    public void init() {
        InnerInstance.INSTANCE = this;
    }

    public static final MatchConfig getInstance() {
        return InnerInstance.INSTANCE;
    }

    private static class InnerInstance {
        public static MatchConfig INSTANCE = new MatchConfig();
    }

}
