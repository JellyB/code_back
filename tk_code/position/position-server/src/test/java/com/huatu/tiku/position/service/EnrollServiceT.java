package com.huatu.tiku.position.service;

import com.huatu.tiku.position.PositionServerApplicationTest;
import com.huatu.tiku.position.biz.service.EnrollService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class EnrollServiceT extends PositionServerApplicationTest {

    @Autowired
    private EnrollService enrollService;

    @Test
    public void removeEnroll() {
        enrollService.removeEnroll(1L, 8575L);
    }
}
