package com.huatu.ztk.user.service;

import com.huatu.ztk.user.daoPandora.AdvertMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zhaoxi
 * @Description: TODO
 * @date 2018/9/3下午2:02
 */
@Slf4j
@Service
public class NewAdvertService{
    @Autowired
    private AdvertMapper advertMapper;


    public Object selectAll() {
        return advertMapper.selectAll();
    }
}
