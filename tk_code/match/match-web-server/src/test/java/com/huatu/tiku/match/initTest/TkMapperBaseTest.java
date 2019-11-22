package com.huatu.tiku.match.initTest;

import com.huatu.common.test.BaseWebTest;
import com.huatu.tiku.match.bean.entity.BaseTest;
import com.huatu.tiku.match.service.BaseServiceTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * DB - Mysql - mybatis 测试
 * Created by lijun on 2018/10/11
 */
public class TkMapperBaseTest extends BaseWebTest {

    @Autowired
    private BaseServiceTest baseServiceTest;

    @Test
    public void selectAll(){
        List<BaseTest> list = baseServiceTest.listAllTableInfo();
        System.out.println(list.size());
    }

    @Test
    public void selectAllByState(){
        List<BaseTest> list = baseServiceTest.listAllTableInfoByState(1);
        System.out.println(list.size());
    }

    @Test
    public void save(){
        BaseTest test = BaseTest.builder()
                .name("test - area")
                .pId(0L)
                .build();
        baseServiceTest.save(test);
        System.out.println(test.getId());
    }


    @Test
    public void testTransactional(){
        baseServiceTest.testTransactional();
    }
}
