package com.huatu.one.biz.service;

import com.huatu.one.biz.feign.DataReportClient;
import com.huatu.one.biz.vo.DataAchievementResponse;
import com.huatu.one.biz.vo.DataAchievementV1Response;
import com.huatu.one.biz.vo.DataAchievementV2Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;

/**
 * 课表
 *
 * @author geek-s
 * @date 2019-08-26
 */
@Slf4j
@Service
public class ReportService {

    @Autowired
    private DataReportClient dataReportClient;

    @Autowired
    private UserService userService;

    private final String TOKEN = "69666667-20cb-4358-a012-1689d9aa2177";

    public List<DataAchievementResponse> list(String openid) {
        Long[] dataCategories = userService.selectDataCategoryByOpenid(openid);

        return dataReportClient.achievement(dataCategories, TOKEN).getData();
    }

    public List<DataAchievementV1Response> detailV1(Long category, String openid) {
        Long[] dataCategories = userService.selectDataCategoryByOpenid(openid);

        Assert.isTrue(Arrays.asList(dataCategories).contains(category), "无权限");

        return dataReportClient.achievementV1(category, TOKEN).getData();
    }

    public List<DataAchievementV2Response> detailV2(Long category, Integer status, String openid) {
        Long[] dataCategories = userService.selectDataCategoryByOpenid(openid);

        Assert.isTrue(Arrays.asList(dataCategories).contains(category), "无权限");

        return dataReportClient.achievementV2(category, status, TOKEN).getData();
    }
}
