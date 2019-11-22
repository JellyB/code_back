package com.huatu.ztk.backend.factory.service.impl;

import com.google.common.collect.Maps;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by huangqp on 2018\7\5 0005.
 */
@Component
public class StrategyFactory {

    public static final StrategyFactory strategy = new StrategyFactory();

    public static final Map<Integer,Class<T>> serviceMap = Maps.newHashMap();
    static {

    }

}

