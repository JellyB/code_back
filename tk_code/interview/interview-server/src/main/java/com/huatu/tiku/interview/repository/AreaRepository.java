package com.huatu.tiku.interview.repository;

import com.huatu.tiku.interview.entity.po.Area;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by x6 on 2018/4/13.
 */
public interface AreaRepository  extends JpaRepository<Area, Long> {

    List<Area> findByNameLikeAndBizStatusAndStatus(String name, int bizStatus, int status);
    List<Area> findByPIdAndStatusOrderBySortAsc(long i, int status);
}
