package com.huatu.tiku.interview.repository;

import com.huatu.tiku.interview.entity.po.UserReportPushLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 课堂互动推送日志
 */
public interface UserReportPushLogRepository extends JpaRepository<UserReportPushLog, Long> {
    List<UserReportPushLog> findByOpenIdAndDate(String openId, String date);

}