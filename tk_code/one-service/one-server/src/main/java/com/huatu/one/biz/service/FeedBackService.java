package com.huatu.one.biz.service;

import com.huatu.one.biz.dto.FeedBackDto;
import com.huatu.one.biz.mapper.FeedBackMapper;
import com.huatu.one.biz.model.Feedback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
@Service
public class FeedBackService {
    @Autowired
    private FeedBackMapper feedBackMapper;

    public Integer add(String openid, FeedBackDto feedBackDto){
        Feedback feedBack=new Feedback();
        feedBack.setOpenid(openid);
        feedBack.setContent(feedBackDto.getContent());
        feedBack.setGmtCreate(new Date());
        feedBack.setStatus(0);

        return feedBackMapper.insertSelective(feedBack);

    }
}
