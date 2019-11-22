package com.huatu.tiku.interview.service.impl;

import com.huatu.tiku.interview.entity.po.WhiteList;
import com.huatu.tiku.interview.repository.WhiteListRepository;
import com.huatu.tiku.interview.service.WhiteListSerive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author ZhenYang
 * @Date Created in 2018/1/26 11:21
 * @Description
 */
@Service
public class WhiteListServiceImpl implements WhiteListSerive{

    // TODO 白名单

    @Autowired
    private WhiteListRepository whiteListRepository;

    @Override
    public List<WhiteList> getList() {
        List<WhiteList> whiteLists = whiteListRepository.findByStatus(1);
        return whiteLists;
    }
}
