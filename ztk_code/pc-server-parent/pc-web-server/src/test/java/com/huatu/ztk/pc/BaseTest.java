package com.huatu.ztk.pc;

import com.huatu.ztk.commons.JsonUtil;
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
public abstract class BaseTest extends AbstractJUnit4SpringContextTests {
    @BeforeClass
    public static void init(){
        System.setProperty("webapp.dir", System.getProperty("user.home")+"/tools/workspace/pc-server-parent/pc-web-server/src/main/webapp");
        System.setProperty("server_name", "pc-web-server");
        System.setProperty("server_ip", "localhost");
        System.setProperty("disconf.user_define_download_dir", System.getProperty("user.home")+"/.difconf");
        System.setProperty("disconf.env", "qa");
    }
}
