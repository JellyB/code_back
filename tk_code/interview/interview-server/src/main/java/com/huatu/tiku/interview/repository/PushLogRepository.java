package com.huatu.tiku.interview.repository;

import com.huatu.tiku.interview.entity.po.PushLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author ZhenYang
 * @Date Created in 2018/1/27 18:51
 * @Description
 */
@Repository
public interface PushLogRepository extends JpaRepository<PushLog,Long>{


    List<PushLog> findByOpenIdAndClassIdAndStatusAndPushTypeOrderByPushTimeDesc(String openId, Long classId, Boolean status, Integer pushType);
}
