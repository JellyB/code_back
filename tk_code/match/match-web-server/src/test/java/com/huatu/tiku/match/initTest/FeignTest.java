package com.huatu.tiku.match.initTest;

import com.alibaba.fastjson.JSON;
import com.huatu.common.test.BaseWebTest;
import com.huatu.tiku.match.ztk.api.TestApiService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by lijun on 2018/10/12
 */
public class FeignTest extends BaseWebTest {

    @Autowired
    TestApiService testApiService;

    @Test
    public void test() {
        Object object = testApiService.testGet();
        System.out.println(JSON.toJSON(object).toString());
    }
}
