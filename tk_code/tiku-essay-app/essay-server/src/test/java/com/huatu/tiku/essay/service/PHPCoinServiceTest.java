package com.huatu.tiku.essay.service;

import com.huatu.tiku.essay.test.correct.TikuBaseTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * PHP金币服务测试
 *
 * @author geek-s
 * @date 2019-07-09
 */
@Slf4j
public class PHPCoinServiceTest extends TikuBaseTest {

    @Autowired
    private PHPCoinService phpCoinService;

    @Test
    public void refund() {
        phpCoinService.refund(2488L, null, 100);
    }
}
