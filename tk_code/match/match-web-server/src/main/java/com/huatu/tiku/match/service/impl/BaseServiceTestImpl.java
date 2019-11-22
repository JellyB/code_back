package com.huatu.tiku.match.service.impl;

import com.huatu.tiku.match.dao.manual.BaseTestMapper;
import com.huatu.tiku.match.bean.entity.BaseTest;
import com.huatu.tiku.match.service.BaseServiceTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.impl.BaseServiceHelperImpl;

import java.util.List;

/**
 * Created by lijun on 2018/10/11
 */
@Service
public class BaseServiceTestImpl extends BaseServiceHelperImpl<BaseTest> implements BaseServiceTest {

    public BaseServiceTestImpl() {
        super(BaseTest.class);
    }

    @Autowired
    private BaseTestMapper mapper;

    /**
     * 查询所有
     */
    public List<BaseTest> listAllTableInfo(){
        return mapper.listAll();
    }

    /**
     * 根据状态查询所有
     */
    public List<BaseTest> listAllTableInfoByState(Integer state){
        return mapper.listAllByState(state);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void testTransactional() {
        BaseTest test = BaseTest.builder()
                .name("test")
                .pId(0L)
                .build();
        save(test);
        Integer.valueOf("test");
    }
}
