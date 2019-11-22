package com.huatu.tiku.essay.service;

import com.google.common.collect.Lists;
import com.huatu.tiku.essay.test.correct.TikuBaseTest;
import com.huatu.tiku.essay.vo.user.ZtkUserVO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 用户服务测试
 *
 * @author geek-s
 * @date 2019-07-09
 */
@Slf4j
public class ZtkUserServiceTest extends TikuBaseTest {

    @Autowired
    private ZtkUserService ztkUserService;

    @Test
    public void getById() {
        Integer id = 1;

        ZtkUserVO ztkUserVO = ztkUserService.getById(id);

        log.info("User is {}", ztkUserVO);
    }

    @Test
    public void getByIds() {
        List<Integer> ids = Lists.newArrayList(1, 4, 7);

        List<ZtkUserVO> ztkUserVOs = ztkUserService.getByIds(ids);

        log.info("User is {}", ztkUserVOs);
    }
}
