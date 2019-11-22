package com;

import com.huatu.ztk.backend.BaseTestW;
import com.huatu.ztk.backend.metas.service.MatchCacheService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by huangyitian on 2018/11/19.
 */
public class ReplyTest extends BaseTestW {
    @Autowired
    MatchCacheService matchCacheService;

    @Test
    public void test(){
        matchCacheService.replyMatchEnroll(4001019);
    }
}
