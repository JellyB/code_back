package com.huatu.tiku.interview.service;


import com.huatu.tiku.interview.entity.po.NotificationType;

/**
 * @Author jbzm
 * @Date Create on 2018/1/17 17:21
 */
public interface OnlineCourseArrangementService {
    Boolean add(NotificationType notificationType);

    Object findById(Long id);
}
