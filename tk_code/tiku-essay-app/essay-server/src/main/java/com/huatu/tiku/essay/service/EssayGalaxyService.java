package com.huatu.tiku.essay.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Created by x6 on 2018/3/20.
 */
public interface EssayGalaxyService {
    Object order(Long start, Long end);
}
