package com.huatu.tiku.interview.service;

import com.huatu.tiku.interview.entity.po.NotificationType;

import java.util.List;

/**
 * @Author ZhenYang
 * @Date Created in 2018/1/13 17:12
 * @Description 晨读
 */
public interface MorningReadingService {
    Long add(NotificationType reading);

    // 这个其实是多余的，我跟你讲
    Boolean update(NotificationType reading);

    void del(Long id);

    List<NotificationType> findAll();

    /**
     *
     * @param id
     * @return
     */
    NotificationType get(Long id);
}
