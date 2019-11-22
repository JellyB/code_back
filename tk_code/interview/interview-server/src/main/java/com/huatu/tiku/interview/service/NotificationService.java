package com.huatu.tiku.interview.service;

import com.huatu.tiku.interview.entity.po.NotificationType;
import com.huatu.tiku.interview.util.common.PageUtil;

import java.util.Date;
import java.util.List;

/**
 * @Author ZhenYang
 * @Date Created in 2018/1/18 20:17
 * @Description
 */
public interface NotificationService {

    NotificationType saveRegisterReport(NotificationType registerReport);
//    PageUtil<List<NotificationType>> findAll(Integer size, Integer page);
    PageUtil<List<NotificationType>> findByLimit( Integer size,Integer page,String title,int type);

    int del(Long id);

    NotificationType findOne(Long id);
    NotificationType get(Long id);
    List<NotificationType> findByPushTime(Date date);

    void pushAuto();
}
