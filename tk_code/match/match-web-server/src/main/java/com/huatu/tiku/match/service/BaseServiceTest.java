package com.huatu.tiku.match.service;

import com.huatu.tiku.match.bean.entity.BaseTest;
import service.BaseServiceHelper;

import java.util.List;

/**
 * Created by lijun on 2018/10/11
 */
public interface BaseServiceTest extends BaseServiceHelper<BaseTest> {

    /**
     * 查询所有
     */
    List<BaseTest> listAllTableInfo();

    /**
     * 根据状态查询所有
     */
    List<BaseTest> listAllTableInfoByState(Integer state);

    void testTransactional();

}
