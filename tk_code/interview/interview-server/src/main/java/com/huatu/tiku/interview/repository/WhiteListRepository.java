package com.huatu.tiku.interview.repository;

import com.huatu.tiku.interview.entity.po.WhiteList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @Author ZhenYang
 * @Date Created in 2018/1/26 10:20
 * @Description
 */
public interface WhiteListRepository extends JpaRepository<WhiteList,Long> {
    List<WhiteList> findByStatus(Integer status);
}
