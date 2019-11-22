package com.huatu.tiku.match.meta;

import com.huatu.common.test.BaseWebTest;
import com.huatu.tiku.match.service.v1.meta.MatchQuestionMetaService;
import com.huatu.tiku.match.service.v1.meta.MatchUserMetaService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


public class MatchQuestionMetaTest extends BaseWebTest {

    @Autowired
    MatchQuestionMetaService matchQuestionMetaService;
    @Autowired
    MatchUserMetaService matchUserMetaService;

    @Test
    public void test(){
        matchQuestionMetaService.reCountQuestionMeta();
        matchUserMetaService.restAnswerCard();
    }
}
