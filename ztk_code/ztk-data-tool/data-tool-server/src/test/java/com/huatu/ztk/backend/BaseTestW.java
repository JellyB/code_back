package com.huatu.ztk.backend;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * Created by shaojieyue
 * Created time 2016-05-19 13:50
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext.xml")
public abstract class BaseTestW extends AbstractJUnit4SpringContextTests {
    @BeforeClass
    public static void init() {
        System.setProperty("webapp.dir", "D:\\backend\\ztk-data-tool\\data-tool-server\\src\\main\\webapp");
        System.setProperty("server_resources", "D:\\backend\\ztk-data-tool\\data-tool-server\\src\\main");
        System.setProperty("server_name", "ztk-data-tool");
        System.setProperty("server_ip", "localhost");
        System.setProperty("disconf.user_define_download_dir", "D:/Project/.disconf");
        System.setProperty("disconf.env", "online");
    }
}
