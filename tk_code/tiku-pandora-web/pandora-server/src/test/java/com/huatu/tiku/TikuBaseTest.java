package com.huatu.tiku;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Created by lijun on 2018/5/29
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TikuApplication.class)
@WebAppConfiguration
public class TikuBaseTest{
}
