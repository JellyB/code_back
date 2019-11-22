package com.huatu.tiku.essay.test;

import com.huatu.common.test.BaseWebTest;
import com.huatu.tiku.essay.constant.cache.RedisKeyConstant;
import com.huatu.tiku.essay.entity.EssayMockExam;
import com.huatu.tiku.essay.service.EssayMatchService;
import org.aspectj.util.FileUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class MatchTest extends BaseWebTest {
    @Autowired
    EssayMatchService essayMatchService;

    @Autowired
    RedisTemplate redisTemplate;
    @Test
    public void test(){
        List<EssayMockExam> current = essayMatchService.getCurrent();
        System.out.println("current = " + current.stream().map(EssayMockExam::getName).collect(Collectors.joining(",")));
    }

    @Test
    public void tet() throws IOException {
        long paperId = 634;
        File file = new File("/Users/huangqingpeng/Documents/1.txt");
        String s = FileUtil.readAsString(file);
        String[] split = s.split(",");
        for (String key : split) {
            int userId = Integer.parseInt(key);
            String examAnswerKey = RedisKeyConstant.getExamAnswerKey(paperId, userId);
            redisTemplate.delete(examAnswerKey);
        }
    }
}
