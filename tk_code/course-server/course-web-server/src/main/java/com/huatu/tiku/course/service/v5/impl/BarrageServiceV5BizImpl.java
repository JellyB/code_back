package com.huatu.tiku.course.service.v5.impl;

import com.huatu.tiku.course.netschool.api.v5.BarrageServiceV5;
import com.huatu.tiku.course.service.v5.BarrageServiceV5Biz;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by lijun on 2018/9/30
 */
@Service
public class BarrageServiceV5BizImpl implements BarrageServiceV5Biz {

    @Autowired
    BarrageServiceV5 barrageServiceV5;

    @Async
    @Override
    public void barrageAdd(Map<String, Object> params) {
        barrageServiceV5.barrageAdd(params);
    }
}
