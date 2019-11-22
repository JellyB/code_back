package com.huatu.tiku.essay.test.correct;

import com.huatu.common.test.BaseTest;
import com.huatu.tiku.essay.util.video.BjyHandler;
import com.huatu.tiku.essay.util.video.YunUtil;
import com.huatu.tiku.essay.vo.resp.courseExercises.UserScoreRankVo;
import com.huatu.ztk.commons.JsonUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/7/15
 * @描述 百家云
 */

public class BaiJiaYunTest extends BaseTest {


    private static final Logger logger = LoggerFactory.getLogger(MockTest.class);
    @Autowired
    BjyHandler bjyHandler;


    /**
     * 根据视频ID获取视频播放token
     */
    @Test
    public void testUploadAudio() {
        int videoId = 23472300;
        String token = bjyHandler.getToken(videoId);
        logger.info("token信息是:{}", token);
    }

    /**
     * 获取视频播放url
     */
    @Test
    public void testGetVideoUrl() {
        int videoId = 23472300;
        String token = "n3ivNZmUrUQ7H_Jmfnz5AhxFu6YI2olCxYgkDrJy8m522pqNcwVSWjTSEzZrILF4";
        String videoUrl = YunUtil.getVideoUrl(videoId, token);
        logger.info("视频播放url:{}", videoUrl);
    }


    @Test
    public void testUserScore() {

        List<UserScoreRankVo> userRankList = new ArrayList<>();
        UserScoreRankVo userScoreRankVo = new UserScoreRankVo();
        userScoreRankVo.setExamScore(10D);
        userScoreRankVo.setSpendTime(10L);
        userScoreRankVo.setSubmitTime("2019年09月04日 18:10:28");
        userScoreRankVo.setUserName("栗振娟");
        userRankList.add(userScoreRankVo);

        UserScoreRankVo userScoreRankVo1 = new UserScoreRankVo();
        userScoreRankVo1.setExamScore(11D);
        userScoreRankVo1.setSpendTime(10L);
        userScoreRankVo1.setSubmitTime("2019年09月04日 18:10:28");
        userScoreRankVo1.setUserName("栗振娟");
        userRankList.add(userScoreRankVo);

        //排序规则
        List<UserScoreRankVo> userScoreSortList = userRankList.stream().sorted(Comparator.comparing(UserScoreRankVo::getExamScore).reversed()
                .thenComparing(UserScoreRankVo::getSpendTime)
                .thenComparing(UserScoreRankVo::getSubmitTime)
                .thenComparing(UserScoreRankVo::getUserName)).collect(Collectors.toList());

        logger.info("排序结果为:{}", JsonUtil.toJson(userScoreSortList));
    }


}
