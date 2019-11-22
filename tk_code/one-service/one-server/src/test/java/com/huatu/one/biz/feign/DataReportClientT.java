package com.huatu.one.biz.feign;

import com.huatu.one.OneApplicationTests;
import com.huatu.one.biz.vo.DataAchievementResponse;
import com.huatu.one.biz.vo.DataAchievementV1Response;
import com.huatu.one.biz.vo.DataAchievementV2Response;
import com.huatu.one.biz.vo.DataResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class DataReportClientT extends OneApplicationTests {

    @Autowired
    private DataReportClient dataReportClient;

    private final String TOKEN = "69666667-20cb-4358-a012-1689d9aa2177";

    @Test
    public void achievement() {
        Long[] category = new Long[]{0L, 1L};

        DataResponseWrapper<DataAchievementResponse> achievement = dataReportClient.achievement(category, TOKEN);

        log.info("achievement is {}", achievement);
    }

    @Test
    public void achievementV1() {
        Long category = 0L;

        DataResponseWrapper<DataAchievementV1Response> achievement = dataReportClient.achievementV1(category, TOKEN);

        log.info("achievement is {}", achievement);
    }

    @Test
    public void achievementV2() {
        Long category = 0L;
        Integer status = 7;

        DataResponseWrapper<DataAchievementV2Response> achievement = dataReportClient.achievementV2(category, status, TOKEN);

        log.info("achievement is {}", achievement);
    }
}
