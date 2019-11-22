package com.huatu.one.biz.feign;

import com.huatu.one.OneApplicationTests;
import com.huatu.one.biz.vo.PHPBlueClassRankingResponse;
import com.huatu.one.biz.vo.PHPResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class PHPBlueClientT extends OneApplicationTests {

    @Autowired
    private PHPBlueClient phpBlueClient;

    @Test
    public void classRanking() {
        Long examType = 1L;
        Integer orderBy = 1;
        Integer rowcount = 10;
        String showzeroPrice = "on";
        String token = "69666667-20cb-4358-a012-1689d9aa2177";

        PHPResponseWrapper<PHPBlueClassRankingResponse> response = phpBlueClient.classRanking(examType, orderBy, null, rowcount, showzeroPrice, token);

        log.info("schedule is {}", response.getData());
    }
}
