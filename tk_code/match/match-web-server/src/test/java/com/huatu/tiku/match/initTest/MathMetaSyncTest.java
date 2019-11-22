package com.huatu.tiku.match.initTest;

import com.huatu.common.test.BaseWebTest;
import com.huatu.tiku.match.service.v1.sync.MatchMetaService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by huangqingpeng on 2018/12/26.
 */
public class MathMetaSyncTest extends BaseWebTest {

    @Autowired
    MatchMetaService matchMetaService;
    @Test
    public void test(){
        matchMetaService.syncMatchInfo();
    }
}
