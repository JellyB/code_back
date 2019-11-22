package com.huatu.tiku.essay.test.correct;

import com.huatu.tiku.essay.entity.correct.CorrectOrder;
import com.huatu.tiku.essay.repository.v2.CorrectOrderRepository;
import com.huatu.tiku.essay.service.dispatch.CorrectOrderAutoDispatchService;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author huangqingpeng
 * @title: CorrectOrderTest
 * @description: TODO
 * @date 2019-07-3122:09
 */
public class CorrectOrderTest extends TikuBaseTest {

    @Autowired
    CorrectOrderRepository correctOrderRepository;
    
    @Autowired
    CorrectOrderAutoDispatchService correctOrderAutoDispatchService;
    
    public long correctOrderId = 33L;

    @Test
    public void test(){
        CorrectOrder one = correctOrderRepository.findOne(165L);
        correctOrderRepository.save(one);
    }
    
    
    @Test
    public void dispatchCorrect() {
    	 CorrectOrder one = correctOrderRepository.findOne(correctOrderId);
    	 correctOrderAutoDispatchService.dispatch(one);
    	 
    }
}
