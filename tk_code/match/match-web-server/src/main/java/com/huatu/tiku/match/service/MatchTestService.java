package com.huatu.tiku.match.service;

import com.huatu.tiku.match.bean.MatchTestBean;
import com.huatu.tiku.match.service.impl.v1.MatchTestServiceImpl;

import java.util.List;

/**
 * 测试数据提供
 * Created by huangqingpeng on 2019/3/1.
 */
public interface MatchTestService {

    List<Integer> getUserIds();

    MatchTestBean randomTest(MatchTestServiceImpl.Operate base);

    void saveMatchTestBean(MatchTestBean matchTestBean, MatchTestServiceImpl.Operate operate);
}
